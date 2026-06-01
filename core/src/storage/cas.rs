//! Content-Addressable Storage (CAS) for large binary assets.
//!
//! Stores media (images, audio, etc.) using SHA-256 hash as the key.
//! References are stored in Loro as `asset://sha256/[hash]` URLs.

use sha2::{Digest, Sha256};
use std::fs;
use std::path::Path;

/// Compute SHA-256 hash of data and return hex-encoded string
#[boltffi::export]
#[must_use]
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
pub fn store_asset(cas_root: &Path, data: &[u8]) -> Result<String, Box<dyn std::error::Error>> {
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
pub fn load_asset(cas_root: &Path, hash: &str) -> Result<Vec<u8>, Box<dyn std::error::Error>> {
    let asset_path = cas_root.join(hash);
    Ok(fs::read(asset_path)?)
}

/// Verify asset exists in CAS.
#[must_use]
pub fn asset_exists(cas_root: &Path, hash: &str) -> bool {
    cas_root.join(hash).exists()
}

#[cfg(test)]
mod tests {
    use super::*;
    use temp_dir::TempDir;

    #[test]
    fn test_compute_asset_hash() {
        let data = b"Hello, Mnemo!";
        let hash = compute_asset_hash(data);

        // SHA256 of "Hello, Mnemo!" is deterministic
        let same_hash = compute_asset_hash(data);
        assert_eq!(hash, same_hash);

        // Different data produces different hash
        let different_hash = compute_asset_hash(b"Different");
        assert_ne!(hash, different_hash);
    }

    #[test]
    fn test_store_and_load_asset() {
        let temp_dir = TempDir::new().unwrap();
        let cas_root = temp_dir.path();

        let data = b"Asset content";
        let url = store_asset(cas_root, data).unwrap();

        assert!(url.starts_with("asset://sha256/"));

        let hash = url.strip_prefix("asset://sha256/").unwrap();
        let loaded = load_asset(cas_root, hash).unwrap();

        assert_eq!(loaded, data);
    }

    #[test]
    fn test_asset_exists() {
        let temp_dir = TempDir::new().unwrap();
        let cas_root = temp_dir.path();

        let data = b"Test data";
        let url = store_asset(cas_root, data).unwrap();
        let hash = url.strip_prefix("asset://sha256/").unwrap();

        assert!(asset_exists(cas_root, hash));
        assert!(!asset_exists(cas_root, "nonexistent"));
    }

    #[test]
    fn test_deduplication() {
        let temp_dir = TempDir::new().unwrap();
        let cas_root = temp_dir.path();

        let data = b"Same content";
        let url1 = store_asset(cas_root, data).unwrap();
        let url2 = store_asset(cas_root, data).unwrap();

        // Same content produces same URL
        assert_eq!(url1, url2);

        // Only one file is stored
        let entries: Vec<_> = fs::read_dir(cas_root)
            .unwrap()
            .filter_map(Result::ok)
            .collect();
        assert_eq!(entries.len(), 1);
    }
}
