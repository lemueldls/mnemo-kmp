#![allow(clippy::ptr_arg)]

//! Content-Addressable Storage (CAS) for large binary assets.
//!
//! Stores media (images, audio, etc.) using SHA-256 hash as the key.
//! References are stored in Loro as `asset://sha256/[hash]` URLs.

use std::{fs, path::PathBuf};

use sha2::{Digest, Sha256};

pub struct Cas {
    root: PathBuf,
}

impl Cas {
    pub fn store(&self, data: &[u8]) -> Result<AssetRef> {
        let hash = compute_asset_hash(data);
        let path = self.root.join(&hash);
        if !path.exists() {
            atomic_write(&path, data)?;
        }
        Ok(AssetRef(format!("asset://sha256/{hash}")))
    }

    pub fn gc(&self, live_hashes: &HashSet<String>) -> Result<usize> {
        let mut removed = 0;
        for entry in fs::read_dir(&self.root)? {
            let path = entry?.path();
            let hash = path.file_name().unwrap().to_string_lossy().to_string();
            if !live_hashes.contains(&hash) {
                fs::remove_file(&path)?;
                removed += 1;
            }
        }
        Ok(removed)
    }
}

/// Compute SHA-256 hash of data and return hex-encoded string
#[must_use]
#[boltffi::export]
pub fn compute_asset_hash(data: &[u8]) -> String {
    let mut hasher = Sha256::new();
    hasher.update(data);
    let result = hasher.finalize();

    hex::encode(result)
}

/// Store an asset file in the CAS directory and return the reference URL.
///
/// The asset is stored at: `{cas_root}/[sha256]`
/// Returns `asset://sha256/[hash]` URL.
pub fn store_asset(cas_root: &PathBuf, data: &[u8]) -> Result<String, Box<dyn std::error::Error>> {
    let hash = compute_asset_hash(data);

    // Ensure CAS directory exists
    fs::create_dir_all(cas_root)?;

    let asset_path = cas_root.join(&hash);

    // Only write if not already present (content-addressed, so same hash = same content)
    if !asset_path.exists() {
        fs::write(&asset_path, data)?;
    }

    Ok(format!("asset://sha256/{hash}"))
}

/// Load asset data from CAS directory.
pub fn load_asset(cas_root: &PathBuf, hash: &str) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let asset_path = cas_root.join(hash);
    Ok(fs::read(asset_path)?)
}

/// Verify asset exists in CAS.
#[must_use]
pub fn asset_exists(cas_root: &PathBuf, hash: &str) -> bool {
    cas_root.join(hash).exists()
}
