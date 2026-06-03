use std::sync::{Arc, LazyLock};

use boltffi::EventSubscription;
use facet::Facet;
use loro::{Container, ContainerTrait, LoroDoc, LoroMap, ToJson};
use tokio_fs_ext as fs;

pub trait StorageItem {
    type Container: ContainerTrait;
}

pub trait StorageMap {
    const KEY: &'static str;
}

#[boltffi::error]
#[derive(Clone)]
pub enum StorageError {
    IOError(String),
    SerializationError,
    DeserializationError,
    LoroError(String),
}

static WORKSPACE_DOC: LazyLock<LoroDoc> = LazyLock::new(LoroDoc::new);

pub struct Settings {
    container: LoroMap,
}

impl Settings {
    const KEY: &'static str = "settings.json";

    #[must_use]
    pub fn new(doc: &LoroDoc) -> Self {
        let container = doc.get_map(Self::KEY);

        Settings { container }
    }

    pub async fn load() -> Result<Self, StorageError> {
        let file = fs::File::open(Self::KEY)
            .await
            .map_err(|err| StorageError::IOError(err.to_string()))?;

        todo!()
    }

    // pub async fn load_from_file(dir: DirectoryHandle) -> Result<Self, StorageError> {
    //     let options = GetFileHandleOptions { create: false };
    //     let file = dir
    //         .get_file_handle_with_options(Self::KEY, &options)
    //         .await
    //         .map_err(|err| StorageError::IOError(err.to_string()))?;
    //     let data = file
    //         .read()
    //         .await
    //         .map_err(|err| StorageError::IOError(err.to_string()))?;

    //     let item = facet_json::from_slice::<SettingsItem>(&data)
    //         .map_err(|_| StorageError::DeserializationError)?;

    //     self.container
    //         .insert("locale", item.locale)
    //         .map_err(|err| StorageError::LoroError(err.to_string()))?;

    //     Ok(())
    // }

    // pub async fn save_to_file(&self, dir: DirectoryHandle) -> Result<(), StorageError> {
    //     let options = GetFileHandleOptions { create: true };
    //     let mut file = dir
    //         .get_file_handle_with_options(Self::KEY, &options)
    //         .await
    //         .map_err(|err| StorageError::IOError(err.to_string()))?;

    //     let write_options = CreateWritableOptions {
    //         keep_existing_data: false,
    //     };
    //     let mut writer = file
    //         .create_writable_with_options(&write_options)
    //         .await
    //         .map_err(|err| StorageError::IOError(err.to_string()))?;

    //     let item = self.item();
    //     let data = facet_json::to_vec(&item).map_err(|_| StorageError::SerializationError)?;

    //     writer
    //         .write_at_cursor_pos(&data)
    //         .await
    //         .map_err(|err| StorageError::IOError(err.to_string()))?;

    //     Ok(())
    // }

    pub fn item(&self) -> Arc<SettingsItem> {
        let default_item = SettingsItem::default();

        Arc::new(SettingsItem {
            locale: match self.container.get("locale") {
                Some(value) => value.get_deep_value().to_json(),
                None => default_item.locale,
            },
        })
    }

    // #[must_use]
    // pub fn subscribe(&self, callback: impl Fn(Arc<SettingsItem>) + Send + Sync + 'static) {
    //     let item = self.item();
    //     let handle = self.container.subscribe(Arc::new(|_| {
    //         callback(item);
    //     }));

    //     if let Some(handle) = handle {
    //         handle.detach();
    //     }
    // }

    // #[must_use]
    // pub fn subscribe(&self) -> Arc<EventSubscription<Arc<SettingsItem>>> {
    //     let subscription = Arc::new(EventSubscription::new(256));

    //     {
    //         // let item = Arc::clone(&self.item);
    //         let subscription = Arc::clone(&subscription);

    //         let handle = self.container.subscribe(Arc::new(move |_| {
    //             subscription.push_event(self.item());
    //         }));

    //         if let Some(handle) = handle {
    //             handle.detach();
    //         }
    //     }

    //     subscription
    // }
}

#[boltffi::data]
#[derive(Facet)]
#[facet(derive(Default))]
pub struct SettingsItem {
    #[facet(default = "en-US")]
    locale: String,
}
