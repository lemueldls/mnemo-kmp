use std::{
    borrow::Cow,
    path::PathBuf,
    sync::{Arc, LazyLock},
};

use boltffi::{EventSubscription, ffi_stream};
use facet::Facet;
use futures::AsyncReadExt;
use loro::{Container, ContainerTrait, ExportMode, LoroDoc, LoroMap, ToJson, VersionVector};

use crate::{
    fs,
    storage::{cas::Cas, error::StorageError},
};

pub struct SpaceStore {
    pub id: String,
    pub title: String,
    doc: LoroDoc,
    // file: fs::File,
}

// #[boltffi::export]
impl SpaceStore {
    pub async fn open(root: PathBuf, cas: Arc<Cas>) -> Result<Self, StorageError> {
        let doc_path = root.join("space.loro");
        let doc = match fs::read_file(doc_path).await {
            Ok(bytes) => LoroDoc::from_snapshot(&bytes).unwrap_or_else(|_| LoroDoc::new()),
            Err(_) => LoroDoc::new(),
        };

        let id = doc
            .get_map("config")
            .get("id")
            .and_then(|v| v.into_value().ok())
            .and_then(|v| v.into_string().ok())
            .map(|v| v.unwrap())
            .ok_or(StorageError::InvalidDoc("missing config.id"))?;
        let title = doc
            .get_map("config")
            .get("title")
            .and_then(|v| v.into_value().ok())
            .and_then(|v| v.into_string().ok())
            .map(|v| v.unwrap())
            .unwrap_or_default();

        Ok(Self { id, title, doc })
    }

    //     pub async fn new(space_id: &str) -> Result<Self, StorageError> {
    //         let mut file = fs::File::open(format!("{space_id}/.mnemo/space.loro"))
    //             .await
    //             .map_err(StorageError::from)?;

    //         let mut bytes = Vec::new();
    //         file.read_to_end(&mut bytes)
    //             .await
    //             .map_err(StorageError::from)?;

    //         let doc = LoroDoc::from_snapshot(&bytes).map_err(StorageError::from)?;

    //         Ok(SpaceStore { doc, file })
    //     }
}

#[boltffi::data]
#[derive(Facet)]
#[facet(derive(Default))]
pub struct SpaceStoreItem {
    #[facet(default = "en-US")]
    locale: String,
}
