use std::path::PathBuf;

use loro::LoroDoc;

use crate::{fs, storage::error::StorageError};

pub struct PersistentStore {
    doc: LoroDoc,
    path: String,
}

impl PersistentStore {
    pub async fn open(path: String) -> Result<Self, StorageError> {
        let doc = match fs::read_file(&path).await {
            Ok(bytes) => LoroDoc::from_snapshot(&bytes).unwrap_or_else(|_| LoroDoc::new()),
            Err(_) => LoroDoc::new(),
        };

        Ok(Self { doc, path })
    }

    pub async fn flush(&self) -> Result<()> {
        let bytes = self.doc.export(ExportMode::Snapshot)?;
        atomic_write(&self.path, &bytes).await
    }

    /// Prune history older than `keep_days`. Shrinks the file significantly
    /// while preserving all current state. After pruning, undo depth and
    /// offline merge window are bounded to that horizon.
    pub fn prune_before(&mut self, keep_days: u32) -> Result<()> {
        let cutoff = SystemTime::now() - Duration::from_secs(keep_days as u64 * 86_400);

        // Walk the oplog to find the latest frontier whose timestamp predates
        // the cutoff. Loro's change metadata includes a lamport timestamp;
        // you'd cross-reference with wall-clock time stored in commit metadata.
        let frontier = self.frontier_before(cutoff)?;

        let bytes = self.doc.export(ExportMode::ShallowSnapshot {
            frontiers: frontier,
        })?;
        atomic_write(&self.path, &bytes)?;
        // Reload so the in-memory doc matches the pruned snapshot.
        self.doc = LoroDoc::from_snapshot(&bytes)?;
        Ok(())
    }

    /// Collapse to current-state-only snapshot. Maximum space savings;
    /// no undo or sync history whatsoever. Useful for archived spaces.
    pub fn prune_all(&mut self) -> Result<()> {
        let frontier = self.doc.oplog_frontiers();
        let bytes = self.doc.export(ExportMode::ShallowSnapshot {
            frontiers: frontier,
        })?;
        atomic_write(&self.path, &bytes)?;
        self.doc = LoroDoc::from_snapshot(&bytes)?;
        Ok(())
    }
}
