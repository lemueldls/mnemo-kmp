use std::{borrow::Cow, sync::Arc};

use boltffi::{EventSubscription, ffi_stream};
use loro::{ExportMode, LoroDoc, ToJson, VersionVector};

use crate::storage::StorageError;

/// Manages the workspace Loro documents.
pub struct WorkspaceDocument {
    doc: LoroDoc,
}

#[boltffi::export]
impl WorkspaceDocument {
    /// Creates a new workspace document from an optional snapshot.
    ///
    /// # Panics
    ///
    /// Panics if the snapshot bytes cannot be decoded as a valid Loro document snapshot.
    #[must_use]
    pub fn new(snapshot: Option<Vec<u8>>) -> Self {
        let doc = match snapshot {
            Some(bytes) => LoroDoc::from_snapshot(&bytes).unwrap(),
            None => LoroDoc::new(),
        };

        WorkspaceDocument { doc }
    }

    pub fn settings(&self) -> String {
        self.doc.get_map("settings").get_value().to_json()
    }

    /// Export a workspace's current snapshot.
    pub fn export_snapshot(&self) -> Result<Vec<u8>, StorageError> {
        self.doc
            .export(ExportMode::Snapshot)
            .map_err(|err| StorageError::LoroError(err.to_string()))
    }

    /// Export updates since a given state vector.
    pub fn export_updates(&self, from_version_vector: &[u8]) -> Result<Vec<u8>, StorageError> {
        let vv = VersionVector::decode(from_version_vector)
            .map_err(|_| StorageError::SerializationError)?;

        self.doc
            .export(ExportMode::Updates {
                from: Cow::Owned(vv),
            })
            .map_err(|err| StorageError::LoroError(err.to_string()))
    }

    /// Import updates and return the new state vector.
    #[must_use]
    pub fn import_updates(&self, delta: &[u8]) -> Vec<u8> {
        if self.doc.import(delta).is_err() {
            return Vec::new();
        }

        self.doc.state_vv().encode()
    }

    #[must_use]
    #[ffi_stream(item = Vec<u8>)]
    pub fn subscribe(&self) -> Arc<EventSubscription<Vec<u8>>> {
        let subscription = Arc::new(EventSubscription::new(256));

        {
            let subscription = Arc::clone(&subscription);
            self.doc
                .subscribe_local_update(Box::new(move |bytes| {
                    subscription.push_event(bytes.clone());
                    subscription.is_active()
                }))
                .detach();
        }

        subscription
    }
}
