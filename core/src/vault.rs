//! XChaCha20-Poly1305 vault – bajtově kompatibilní s Android/iOS `SodiumVault`
//! (`"sv1:" || base64(nonce24 || ct || tag16)`, AD = `"elfall/v1/" || pole`).
//! Integrita a vazba na pole: viz `proofs/easycrypt/SodiumVault.ec`.
use crate::CoreError;
use base64::{engine::general_purpose::STANDARD, Engine};
use chacha20poly1305::aead::{Aead, KeyInit, Payload};
use chacha20poly1305::{XChaCha20Poly1305, XNonce};
use zeroize::Zeroizing;

pub const PREFIX: &str = "sv1:";
const NONCE: usize = 24;
const TAG: usize = 16;

fn ad(field: &str) -> Vec<u8> {
    [b"elfall/v1/".as_slice(), field.as_bytes()].concat()
}

fn cipher(key: &[u8]) -> Result<XChaCha20Poly1305, CoreError> {
    if key.len() != 32 {
        return Err(CoreError::InvalidLength);
    }
    Ok(XChaCha20Poly1305::new_from_slice(key).map_err(|_| CoreError::InvalidLength)?)
}

pub fn new_key() -> Result<Zeroizing<[u8; 32]>, CoreError> {
    let mut k = Zeroizing::new([0u8; 32]);
    getrandom::getrandom(&mut k[..]).map_err(|_| CoreError::Rng)?;
    Ok(k)
}

pub fn seal(key: &[u8], field: &str, plaintext: &str) -> Result<String, CoreError> {
    let mut nonce = [0u8; NONCE];
    getrandom::getrandom(&mut nonce).map_err(|_| CoreError::Rng)?;
    seal_with_nonce(key, field, plaintext, &nonce)
}

/// Jen pro testovací vektory – v produkci vždy `seal` (náhodný nonce).
pub fn seal_with_nonce(key: &[u8], field: &str, plaintext: &str, nonce: &[u8; NONCE]) -> Result<String, CoreError> {
    let a = ad(field);
    let ct = cipher(key)?
        .encrypt(XNonce::from_slice(nonce), Payload { msg: plaintext.as_bytes(), aad: &a })
        .map_err(|_| CoreError::AuthFailed)?;
    let mut raw = Vec::with_capacity(NONCE + ct.len());
    raw.extend_from_slice(nonce);
    raw.extend_from_slice(&ct);
    Ok(format!("{PREFIX}{}", STANDARD.encode(raw)))
}

/// `Err(AuthFailed)` při změně dat, jiném klíči nebo jiném poli.
pub fn open(key: &[u8], field: &str, sealed: &str) -> Result<Zeroizing<String>, CoreError> {
    let b64 = sealed.strip_prefix(PREFIX).ok_or(CoreError::AuthFailed)?;
    let raw = STANDARD.decode(b64).map_err(|_| CoreError::AuthFailed)?;
    if raw.len() < NONCE + TAG {
        return Err(CoreError::AuthFailed);
    }
    let a = ad(field);
    let pt = Zeroizing::new(
        cipher(key)?
            .decrypt(XNonce::from_slice(&raw[..NONCE]), Payload { msg: &raw[NONCE..], aad: &a })
            .map_err(|_| CoreError::AuthFailed)?,
    );
    let s = std::str::from_utf8(&pt).map_err(|_| CoreError::AuthFailed)?;
    Ok(Zeroizing::new(s.to_owned()))
}
