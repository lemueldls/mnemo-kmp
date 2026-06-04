use std::io;

use facet::Facet;
use facet_error as error;
use loro::LoroError;

use crate::fs::FsError;

#[boltffi::error]
#[derive(Facet, Debug, Clone)]
#[facet(derive(Error))]
pub enum StorageError {
    LoroError(String),
    #[facet(error::from)]
    FsError(FsError),
    #[facet(error::from)]
    IOError(String),
}

impl From<LoroError> for StorageError {
    fn from(err: LoroError) -> Self {
        StorageError::LoroError(err.to_string())
    }
}

impl From<FsError> for StorageError {
    fn from(err: FsError) -> Self {
        StorageError::FsError(err)
    }
}

impl From<io::Error> for StorageError {
    fn from(err: io::Error) -> Self {
        StorageError::IOError(err.to_string())
    }
}
