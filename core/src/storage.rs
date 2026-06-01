//! Space-centric CRDT state management using Loro.
//!
//! This module provides a registry for managing independent `LoroDoc` instances,
//! one per Space. It handles snapshot persistence, state vector tracking, and
//! merging of remote updates.
use dashmap::DashMap;
use loro::{ExportMode, LoroDoc, VersionVector};
use std::fs;
use std::{borrow::Cow, path::PathBuf};

pub mod cas;
pub mod metadata;
pub mod schema;

#[derive(Clone)]
#[boltffi::error]
pub enum StorageError {
    SpaceNotFound,
    IOError,
    MergeConflict,
    InvalidSnapshot,
    SerializationError,
    LoroError(String),
}

/// Manages all open Loro documents.
pub struct SpaceRegistry {
    spaces: DashMap<String, LoroDoc>,
}

#[boltffi::export]
impl SpaceRegistry {
    #[must_use]
    pub fn new() -> Self {
        SpaceRegistry {
            spaces: DashMap::new(),
        }
    }

    /// Initialize a space from an optional snapshot
    pub fn initialize_space(
        &self,
        space_id: &str,
        snapshot_bytes: &[u8],
    ) -> Result<(), StorageError> {
        let doc = LoroDoc::new();

        // If snapshot provided, import it
        if !snapshot_bytes.is_empty()
            && let Err(_) = doc.import(snapshot_bytes)
        {
            return Err(StorageError::InvalidSnapshot);
        }

        self.spaces.insert(space_id.to_string(), doc);

        Ok(())
    }

    /// Export a space's current snapshot
    #[must_use]
    pub fn export_snapshot(&self, space_id: &str) -> Vec<u8> {
        self.spaces.get(space_id).map_or_else(Vec::new, |doc| {
            doc.export(ExportMode::Snapshot).unwrap_or_default()
        })
    }

    /// Export updates since a given state vector
    #[must_use]
    pub fn export_updates(&self, space_id: &str, from_version_vector: &[u8]) -> Vec<u8> {
        self.spaces.get(space_id).map_or_else(Vec::new, |doc| {
            VersionVector::decode(from_version_vector).map_or_else(
                |_| Vec::new(),
                |vv| {
                    doc.export(ExportMode::Updates {
                        from: Cow::Owned(vv),
                    })
                    .unwrap_or_default()
                },
            )
        })
    }

    /// Import updates and return the new state vector
    #[must_use]
    pub fn import_updates(&self, space_id: &str, delta: &[u8]) -> Vec<u8> {
        if let Some(doc) = self.spaces.get(space_id) {
            if doc.import(delta).is_err() {
                return Vec::new();
            }

            doc.state_vv().encode()
        } else {
            Vec::new()
        }
    }

    /// Reconcile an external filesystem edit with the CRDT state
    pub fn reconcile_filesystem_edit(
        &self,
        space_id: &str,
        date: &str,
        file_content: &str,
    ) -> Result<(), StorageError> {
        self.spaces
            .get(space_id)
            .map_or(
                Err(StorageError::SpaceNotFound),
                |doc| match schema::set_daily_entry(&doc, date, file_content) {
                    Ok(()) => Ok(()),
                    Err(err) => Err(StorageError::LoroError(err.to_string())),
                },
            )
    }

    /// Get daily entry content
    pub fn get_daily_entry(&self, space_id: &str, date: &str) -> String {
        self.spaces.get(space_id).map_or_else(String::new, |doc| {
            schema::get_daily_entry(&doc, date).unwrap_or_default()
        })
    }

    /// Set daily entry content
    pub fn set_daily_entry(
        &self,
        space_id: &str,
        date: &str,
        text: &str,
    ) -> Result<(), StorageError> {
        self.spaces
            .get(space_id)
            .map_or(
                Err(StorageError::SpaceNotFound),
                |doc| match schema::set_daily_entry(&doc, date, text) {
                    Ok(()) => Ok(()),
                    Err(err) => Err(StorageError::LoroError(err.to_string())),
                },
            )
    }

    /// Save snapshot to disk atomically
    pub fn save_snapshot(&self, space_id: &str, path: &PathBuf) -> Result<(), StorageError> {
        if let Some(doc) = self.spaces.get(space_id) {
            let snapshot = doc.export(ExportMode::Snapshot).unwrap_or_default();

            // Ensure parent directory exists
            if let Some(parent) = path.parent()
                && let Err(_) = fs::create_dir_all(parent)
            {
                return Err(StorageError::IOError);
            }

            // Write to temp file, then rename (atomic)
            let temp_path = path.with_extension("tmp");
            if fs::write(&temp_path, snapshot).is_err() {
                return Err(StorageError::IOError);
            }

            if fs::rename(&temp_path, path).is_err() {
                let _ = fs::remove_file(&temp_path);
                return Err(StorageError::IOError);
            }

            Ok(())
        } else {
            Err(StorageError::SpaceNotFound)
        }
    }

    /// Load snapshot from disk and initialize space
    pub fn load_from_disk(&self, space_id: &str, path: &PathBuf) -> Result<(), StorageError> {
        let snapshot_bytes = fs::read(path).unwrap_or_default();

        self.initialize_space(space_id, &snapshot_bytes)
    }

    /// Unload a space from memory
    pub fn unload_space(&self, space_id: &str) -> Result<(), StorageError> {
        if self.spaces.remove(space_id).is_some() {
            Ok(())
        } else {
            Err(StorageError::SpaceNotFound)
        }
    }
}

impl Default for SpaceRegistry {
    fn default() -> Self {
        Self::new()
    }
}
