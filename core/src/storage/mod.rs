//! CRDT state management using Loro.

pub mod cas;
pub mod space;
pub mod workspace;

#[boltffi::error]
#[derive(Clone)]
pub enum StorageError {
    SpaceNotFound,
    IOError,
    MergeConflict,
    InvalidSnapshot,
    SerializationError,
    LoroError(String),
}
