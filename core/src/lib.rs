use std::{ops::Range, path::PathBuf};

pub mod editor;
pub mod storage;
pub mod fs;

#[boltffi::data]
pub struct RangeUsize {
    pub start: usize,
    pub end: usize,
}

boltffi::custom_type! {
    pub Range,
    remote = Range<usize>,
    repr = RangeUsize,
    into_ffi = |range: &Range<usize>| RangeUsize { start: range.start, end: range.end },
    try_from_ffi = |range: RangeUsize| Ok(Range { start: range.start, end: range.end }),
}

boltffi::custom_type! {
    pub PathBuf,
    remote = PathBuf,
    repr = String,
    into_ffi = |path: &PathBuf| path.to_string_lossy().to_string(),
    try_from_ffi = |s: String| Ok(PathBuf::from(s)),
}
