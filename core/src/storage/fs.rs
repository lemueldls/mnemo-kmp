use std::{
    fs::{self, File},
    io::{self, Read, Write},
    num::TryFromIntError,
    path::PathBuf,
    sync::{
        Arc, OnceLock,
        atomic::{AtomicUsize, Ordering},
    },
};

use boltffi::{EventSubscription, ffi_stream};
use dashmap::DashMap;
use facet::Facet;
use notify::{Config, Event, RecommendedWatcher, RecursiveMode, Watcher};

static ROOT_DIR: OnceLock<PathBuf> = OnceLock::new();
static NEXT_WATCHER_ID: AtomicUsize = AtomicUsize::new(1);

static WATCHERS: OnceLock<DashMap<usize, notify::RecommendedWatcher>> = OnceLock::new();

fn get_watchers() -> &'static DashMap<usize, notify::RecommendedWatcher> {
    WATCHERS.get_or_init(DashMap::new)
}

#[boltffi::error]
#[derive(Facet, Debug, Clone)]
#[facet(derive(Error))]
pub enum FsError {
    NotFound(String),
    PermissionDenied(String),
    AlreadyExists(String),
    IOError(String),
    NotInitialized,
    TryFromIntError,
}

impl From<io::Error> for FsError {
    fn from(err: io::Error) -> Self {
        match err.kind() {
            io::ErrorKind::NotFound => FsError::NotFound(err.to_string()),
            io::ErrorKind::PermissionDenied => FsError::PermissionDenied(err.to_string()),
            io::ErrorKind::AlreadyExists => FsError::AlreadyExists(err.to_string()),
            _ => FsError::IOError(err.to_string()),
        }
    }
}

impl From<TryFromIntError> for FsError {
    fn from(_: TryFromIntError) -> Self {
        FsError::TryFromIntError
    }
}

#[boltffi::data]
pub struct FsEntry {
    pub name: String,
    pub path: String,
    pub is_dir: bool,
    pub size_bytes: usize,
}

#[boltffi::data]
pub struct FsWatchEvent {
    pub path: String,
    pub event_type: String,
}

pub struct FsWatcher {
    subscription: Arc<EventSubscription<FsWatchEvent>>,
}

impl FsWatcher {
    #[must_use]
    pub fn new(path: &PathBuf) -> Self {
        let subscription = Arc::new(EventSubscription::new(256));

        if let Ok(resolved) = resolve_path(path) {
            let sub_id = NEXT_WATCHER_ID.fetch_add(1, Ordering::SeqCst);
            let sub = Arc::clone(&subscription);

            let watcher_result = RecommendedWatcher::new(
                move |res: Result<Event, notify::Error>| {
                    if !sub.is_active() {
                        let sub_id = sub_id;
                        std::thread::spawn(move || {
                            get_watchers().remove(&sub_id);
                        });

                        return;
                    }
                    if let Ok(event) = res {
                        let event_type = match event.kind {
                            notify::EventKind::Create(_) => "Create".to_string(),
                            notify::EventKind::Modify(_) => "Modify".to_string(),
                            notify::EventKind::Remove(_) => "Delete".to_string(),
                            _ => "Any".to_string(),
                        };

                        for p in event.paths {
                            sub.push_event(FsWatchEvent {
                                path: p.to_string_lossy().into_owned(),
                                event_type: event_type.clone(),
                            });
                        }
                    }
                },
                Config::default(),
            );

            if let Ok(mut watcher) = watcher_result
                && watcher.watch(&resolved, RecursiveMode::Recursive).is_ok()
            {
                get_watchers().insert(sub_id, watcher);
            }
        }

        FsWatcher { subscription }
    }

    #[must_use]
    #[ffi_stream(item = FsWatchEvent)]
    pub fn subscribe(&self) -> Arc<EventSubscription<FsWatchEvent>> {
        Arc::clone(&self.subscription)
    }
}

#[boltffi::export]
pub fn init_fs(root_path: PathBuf) -> Result<(), FsError> {
    fs::create_dir_all(&root_path)?;

    ROOT_DIR
        .set(root_path)
        .map_err(|_| FsError::IOError("FS already initialized".to_string()))?;

    Ok(())
}

pub fn resolve_path(path: &PathBuf) -> Result<PathBuf, FsError> {
    let root = ROOT_DIR.get().ok_or(FsError::NotInitialized)?;
    Ok(root.join(path))
}

pub fn read_file(path: &PathBuf) -> Result<Vec<u8>, FsError> {
    let resolved = resolve_path(path)?;
    let mut file = File::open(resolved).map_err(FsError::from)?;
    let mut contents = Vec::new();
    file.read_to_end(&mut contents).map_err(FsError::from)?;

    Ok(contents)
}

pub fn write_file(path: &PathBuf, contents: Vec<u8>) -> Result<(), FsError> {
    let resolved = resolve_path(path)?;
    if let Some(parent) = resolved.parent() {
        fs::create_dir_all(parent).map_err(FsError::from)?;
    }

    let mut file = File::create(resolved).map_err(FsError::from)?;
    file.write_all(&contents).map_err(FsError::from)?;

    Ok(())
}

pub fn write_file_and_sync(path: &PathBuf, contents: Vec<u8>) -> Result<(), FsError> {
    let resolved = resolve_path(path)?;
    if let Some(parent) = resolved.parent() {
        fs::create_dir_all(parent).map_err(FsError::from)?;
    }

    let mut file = File::create(resolved).map_err(FsError::from)?;
    file.write_all(&contents).map_err(FsError::from)?;
    file.sync_all().map_err(FsError::from)?;

    Ok(())
}

pub fn sync_file(path: &PathBuf) -> Result<(), FsError> {
    let resolved = resolve_path(path)?;
    let file = File::open(resolved).map_err(FsError::from)?;
    file.sync_all().map_err(FsError::from)?;

    Ok(())
}

pub fn delete_file(path: &PathBuf) -> Result<(), FsError> {
    let resolved = resolve_path(path)?;
    fs::remove_file(resolved).map_err(FsError::from)?;

    Ok(())
}

pub fn create_dir(path: &PathBuf) -> Result<(), FsError> {
    let resolved = resolve_path(path)?;
    fs::create_dir_all(resolved).map_err(FsError::from)?;

    Ok(())
}

pub fn exists(path: &PathBuf) -> Result<bool, FsError> {
    let resolved = resolve_path(path)?;

    match fs::metadata(resolved) {
        Ok(_) => Ok(true),
        Err(err) if err.kind() == io::ErrorKind::NotFound => Ok(false),
        Err(err) => Err(FsError::from(err)),
    }
}

pub fn list_dir(path: &PathBuf) -> Result<Vec<FsEntry>, FsError> {
    let resolved = resolve_path(path)?;
    let mut dir = fs::read_dir(resolved).map_err(FsError::from)?;
    let mut entries = Vec::new();

    while let Some(entry) = dir.next().transpose().map_err(FsError::from)? {
        let name = entry.file_name().to_string_lossy().into_owned();
        let metadata = entry.metadata().map_err(FsError::from)?;
        entries.push(FsEntry {
            name,
            path: entry.path().to_string_lossy().into_owned(),
            is_dir: metadata.is_dir(),
            size_bytes: usize::try_from(metadata.len()).map_err(FsError::from)?,
        });
    }

    Ok(entries)
}
