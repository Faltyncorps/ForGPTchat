//! Argon2id (crate `argon2`), OWASP: m = 19 MiB, t = 2, p = 1. PHC řetězec je kompatibilní
//! s libsodium `crypto_pwhash_str_verify` (Android/iOS `PinHasher`).
use crate::CoreError;
use argon2::password_hash::{PasswordHash, PasswordHasher, PasswordVerifier, SaltString};
use argon2::{Algorithm, Argon2, Params, Version};

pub const M_KIB: u32 = 19_456;
pub const T: u32 = 2;
pub const P: u32 = 1;

fn hasher(m: u32, t: u32) -> Result<Argon2<'static>, CoreError> {
    let p = Params::new(m, t, P, Some(32)).map_err(|_| CoreError::Hash)?;
    Ok(Argon2::new(Algorithm::Argon2id, Version::V0x13, p))
}

pub fn hash(pin: &str) -> Result<String, CoreError> {
    hash_with(pin, M_KIB, T)
}

pub fn hash_with(pin: &str, m: u32, t: u32) -> Result<String, CoreError> {
    let mut salt = [0u8; 16];
    getrandom::getrandom(&mut salt).map_err(|_| CoreError::Rng)?;
    let salt = SaltString::encode_b64(&salt).map_err(|_| CoreError::Hash)?;
    Ok(hasher(m, t)?.hash_password(pin.as_bytes(), &salt).map_err(|_| CoreError::Hash)?.to_string())
}

pub fn verify(pin: &str, phc: &str) -> bool {
    let Ok(h) = PasswordHash::new(phc) else { return false };
    if h.algorithm.as_str() != "argon2id" {
        return false;
    }
    Argon2::default().verify_password(pin.as_bytes(), &h).is_ok()
}

/// true, pokud hash používá slabší parametry než aktuální politika.
pub fn needs_rehash(phc: &str) -> bool {
    let Ok(h) = PasswordHash::new(phc) else { return true };
    let Ok(p) = Params::try_from(&h) else { return true };
    h.algorithm.as_str() != "argon2id" || p.m_cost() < M_KIB || p.t_cost() < T
}
