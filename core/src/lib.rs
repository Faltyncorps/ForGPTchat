//! ELFALL krypto jádro (fáze 2 ZERO-TRUST-ROADMAP). Jediná implementace sdílená
//! Androidem/iOS (UniFFI, feature `ffi`) a webem (wasm-bindgen, feature `wasm`).
#![forbid(unsafe_code)]
#![deny(clippy::unwrap_used, clippy::expect_used, clippy::panic)]

pub mod framing;
pub mod pin;
pub mod sign;
pub mod vault;

#[cfg(feature = "ffi")]
mod ffi;
#[cfg(feature = "wasm")]
mod wasm;

#[cfg(feature = "ffi")]
uniffi::setup_scaffolding!();

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
#[cfg_attr(feature = "ffi", derive(uniffi::Error))]
pub enum CoreError {
    InvalidLength,
    InvalidKey,
    MessageTooLong,
    BadFrame,
    AuthFailed,
    Hash,
    Rng,
}

impl core::fmt::Display for CoreError {
    fn fmt(&self, f: &mut core::fmt::Formatter<'_>) -> core::fmt::Result {
        write!(f, "{self:?}")
    }
}
impl std::error::Error for CoreError {}
