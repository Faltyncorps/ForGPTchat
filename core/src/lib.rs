//! ELFALL krypto jádro (fáze 2 ZERO-TRUST-ROADMAP). Jediná implementace sdílená
//! Androidem/iOS (UniFFI, feature `ffi`) a webem (wasm-bindgen, feature `wasm`).
#![deny(unsafe_code)] // výjimka jen v modulu `sodium` (FFI do libsodium)
#![deny(clippy::unwrap_used, clippy::expect_used, clippy::panic)]

pub mod framing;
#[cfg(not(target_arch = "wasm32"))]
pub mod pin;
#[cfg(not(target_arch = "wasm32"))]
pub mod sign;
#[cfg(target_arch = "wasm32")]
#[path = "sign_wasm.rs"]
pub mod sign;
#[cfg(not(target_arch = "wasm32"))]
mod sodium;
#[cfg(not(target_arch = "wasm32"))]
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
