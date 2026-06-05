use std::{
    path::{Path, PathBuf},
    sync::{Arc, mpsc},
};

use boltffi::{CustomFfiConvertible, EventSubscription, custom_ffi, ffi_stream};
use facet::Facet;
use loro::{ContainerTrait, LoroMap, LoroMapValue, LoroStringValue, LoroValue, ToJson};
use notify::{RecursiveMode, Watcher};
use rustc_hash::FxHashMap;

use crate::storage::fs;

#[derive(Debug)]
pub struct Spaces {
    pub container: LoroMap,
    pub item: Vec<SpacesItem>,
}

impl Spaces {
    pub fn new(container: LoroMap) -> Self {
        let path = PathBuf::from("spaces.json");

        let spaces = if let Ok(file) = fs::read_file(&path) {
            facet_json::from_slice::<FxHashMap<String, SpaceItem>>(&file)
                .expect("Failed to parse spaces.json")
        } else {
            FxHashMap::default()
        };

        for (id, space) in &spaces {
            container
                .insert(id, space.to_loro_value())
                .expect("Failed to insert space into LoroMap");
        }

        let json = container.get_value().to_json_pretty();
        fs::write_file(&path, json.into_bytes()).expect("Failed to write spaces.json");

        let item = spaces
            .into_iter()
            .map(|(key, value)| SpacesItem { key, value })
            .collect();

        Self { container, item }
    }
}

#[boltffi::export]
impl Spaces {
    #[ffi_stream(item = ())]
    pub fn subscribe(&self) -> Arc<EventSubscription<()>> {
        let subscription = Arc::new(EventSubscription::new(256));

        {
            let subscription = Arc::clone(&subscription);
            let mut watcher =
                notify::recommended_watcher(move |res: notify::Result<notify::Event>| {
                    println!("File change detected: {res:?}");
                    match res {
                        Ok(event) => {
                            if let notify::EventKind::Modify(_) = event.kind {
                                subscription.push_event(());
                            }
                        }
                        Err(err) => eprintln!("Watch error: {err:?}"),
                    }
                })
                .expect("Failed to create file watcher");

            let path =
                fs::resolve_path(&PathBuf::from("spaces.json")).expect("Failed to resolve path");
            watcher
                .watch(&path, RecursiveMode::NonRecursive)
                .expect("Failed to watch spaces.json");
        }

        let container = self.container.clone();
        let handle = self.container.subscribe(Arc::new(move |_diff| {
            println!("Container changed, writing to spaces.json");
            let path = PathBuf::from("spaces.json");
            let json = container.get_value().to_json_pretty();
            fs::write_file(&path, json.into_bytes()).expect("Failed to write spaces.json");
        }));

        if let Some(h) = handle {
            h.detach();
        }

        subscription
    }
}

#[boltffi::data]
#[derive(Debug)]
pub struct SpacesItem {
    pub key: String,
    pub value: SpaceItem,
}

#[boltffi::data]
#[derive(Facet, Debug)]
pub struct SpaceItem {
    pub id: String,
    pub name: String,
    pub color: String,
    pub icon: String,
}

impl SpaceItem {
    #[must_use]
    pub fn to_loro_value(&self) -> LoroValue {
        LoroValue::Map(LoroMapValue::from(FxHashMap::from_iter([
            (
                "id".to_owned(),
                LoroValue::String(LoroStringValue::from(self.id.clone())),
            ),
            (
                "name".to_owned(),
                LoroValue::String(LoroStringValue::from(self.name.clone())),
            ),
            (
                "color".to_owned(),
                LoroValue::String(LoroStringValue::from(self.color.clone())),
            ),
            (
                "icon".to_owned(),
                LoroValue::String(LoroStringValue::from(self.icon.clone())),
            ),
        ])))
    }
}
