use std::ops::Range;
use std::path::PathBuf;

pub mod editor;
pub mod storage;

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
    try_from_ffi = |s| Ok(PathBuf::from(s)),
}

#[macro_export]
macro_rules! log {
    ($($e:tt)*) => {
        #[cfg(target_arch="wasm32")]
        $crate::log(&format!($($e)*));
        #[cfg(not(target_arch="wasm32"))]
        eprintln!($($e)*);
    };
}

#[macro_export]
macro_rules! debug {
    ($($e:tt)*) => {
        #[cfg(target_arch="wasm32")]
        $crate::debug(&format!($($e)*));
        #[cfg(not(target_arch="wasm32"))]
        eprintln!($($e)*);
    };
}

#[macro_export]
macro_rules! error {
    ($($e:tt)*) => {
        #[cfg(target_arch="wasm32")]
        $crate::error(&format!($($e)*));
        #[cfg(not(target_arch="wasm32"))]
        eprintln!($($e)*);
    };
}

#[macro_export]
macro_rules! group {
    ($($e:tt)*) => {
        #[cfg(target_arch="wasm32")]
        $crate::group(&format!($($e)*));
        #[cfg(not(target_arch="wasm32"))]
        eprintln!($($e)*);
    };
}

#[macro_export]
macro_rules! group_end {
    ($($e:tt)*) => {
        #[cfg(target_arch="wasm32")]
        $crate::group_end(&format!($($e)*));
        #[cfg(not(target_arch="wasm32"))]
        eprintln!($($e)*);
    };
}
