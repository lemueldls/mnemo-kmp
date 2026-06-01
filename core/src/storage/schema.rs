//! Loro document schema and initialization.
//!
//! Defines the structure of each Space's `LoroDoc`:
//! - `daily_notes`: `LoroMap`<`date` (YYYY-MM-DD) → `LoroText`>
//! - `tasks`: `LoroList`<`LoroMap` with `id`, `title`, `completed`, `created_at`>
//! - `sticky_notes`: `LoroMap`<`UUID` → `LoroMap` with `x`, `y`, `text`>

use loro::{LoroDoc, LoroResult};

/// Get or create a daily entry `LoroText` and return its content.
#[must_use]
pub fn get_daily_entry(doc: &LoroDoc, date: &str) -> Option<String> {
    let daily_notes = doc.get_map("daily_notes");

    if let Some(entry_value) = daily_notes.get(date)
        && let Some(text_ref) = entry_value.as_value()
    {
        return text_ref.as_string().map(|s| s.to_string());
    }

    None
}

/// Set a daily entry with text content.
pub fn set_daily_entry(doc: &LoroDoc, date: &str, text: &str) -> LoroResult<()> {
    let daily_notes = doc.get_map("daily_notes");
    daily_notes.insert(date, text)
}
