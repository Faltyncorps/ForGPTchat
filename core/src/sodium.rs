//! Tenká bezpečná vrstva nad libsodium (libsodium-sys-stable). Jediné místo s `unsafe`;
//! každé volání kontroluje délky bufferů před předáním do C.
#![allow(unsafe_code)]
use crate::CoreError;
use libsodium_sys as s;
use std::sync::OnceLock;

static INIT: OnceLock<bool> = OnceLock::new();

pub fn init() -> Result<(), CoreError> {
    // sodium_init: 0 = ok, 1 = už inicializováno, -1 = chyba
    if *INIT.get_or_init(|| unsafe { s::sodium_init() } >= 0) { Ok(()) } else { Err(CoreError::Rng) }
}

pub fn random(buf: &mut [u8]) -> Result<(), CoreError> {
    init()?;
    unsafe { s::randombytes_buf(buf.as_mut_ptr().cast(), buf.len()) };
    Ok(())
}

// ---- Ed25519 (crypto_sign) ----
pub const SIGN_SEED: usize = 32;
pub const SIGN_PK: usize = 32;
pub const SIGN_SK: usize = 64;
pub const SIGN_BYTES: usize = 64;

pub fn sign_seed_keypair(seed: &[u8; SIGN_SEED]) -> Result<([u8; SIGN_PK], zeroize::Zeroizing<[u8; SIGN_SK]>), CoreError> {
    init()?;
    let mut pk = [0u8; SIGN_PK];
    let mut sk = zeroize::Zeroizing::new([0u8; SIGN_SK]);
    let r = unsafe { s::crypto_sign_seed_keypair(pk.as_mut_ptr(), sk.as_mut_ptr(), seed.as_ptr()) };
    if r == 0 { Ok((pk, sk)) } else { Err(CoreError::InvalidKey) }
}

pub fn sign_detached(sk: &[u8; SIGN_SK], msg: &[u8]) -> Result<[u8; SIGN_BYTES], CoreError> {
    init()?;
    let mut sig = [0u8; SIGN_BYTES];
    let r = unsafe { s::crypto_sign_detached(sig.as_mut_ptr(), std::ptr::null_mut(), msg.as_ptr(), msg.len() as u64, sk.as_ptr()) };
    if r == 0 { Ok(sig) } else { Err(CoreError::InvalidKey) }
}

pub fn verify_detached(pk: &[u8; SIGN_PK], msg: &[u8], sig: &[u8; SIGN_BYTES]) -> Result<bool, CoreError> {
    init()?;
    Ok(unsafe { s::crypto_sign_verify_detached(sig.as_ptr(), msg.as_ptr(), msg.len() as u64, pk.as_ptr()) } == 0)
}

// ---- XChaCha20-Poly1305-IETF ----
pub const AEAD_KEY: usize = 32;
pub const AEAD_NONCE: usize = 24;
pub const AEAD_TAG: usize = 16;

pub fn aead_encrypt(key: &[u8; AEAD_KEY], nonce: &[u8; AEAD_NONCE], msg: &[u8], ad: &[u8]) -> Result<Vec<u8>, CoreError> {
    init()?;
    let mut c = vec![0u8; msg.len() + AEAD_TAG];
    let mut clen: u64 = 0;
    let r = unsafe {
        s::crypto_aead_xchacha20poly1305_ietf_encrypt(c.as_mut_ptr(), &mut clen, msg.as_ptr(), msg.len() as u64,
            ad.as_ptr(), ad.len() as u64, std::ptr::null(), nonce.as_ptr(), key.as_ptr())
    };
    if r != 0 || clen as usize != c.len() { return Err(CoreError::AuthFailed); }
    Ok(c)
}

pub fn aead_decrypt(key: &[u8; AEAD_KEY], nonce: &[u8; AEAD_NONCE], c: &[u8], ad: &[u8]) -> Result<zeroize::Zeroizing<Vec<u8>>, CoreError> {
    init()?;
    if c.len() < AEAD_TAG { return Err(CoreError::AuthFailed); }
    let mut m = zeroize::Zeroizing::new(vec![0u8; c.len() - AEAD_TAG]);
    let mut mlen: u64 = 0;
    let r = unsafe {
        s::crypto_aead_xchacha20poly1305_ietf_decrypt(m.as_mut_ptr(), &mut mlen, std::ptr::null_mut(), c.as_ptr(), c.len() as u64,
            ad.as_ptr(), ad.len() as u64, nonce.as_ptr(), key.as_ptr())
    };
    if r != 0 || mlen as usize != m.len() { return Err(CoreError::AuthFailed); }
    Ok(m)
}

// ---- Argon2id (crypto_pwhash_str) ----
pub fn pwhash_str(pw: &[u8], ops: u64, mem_bytes: usize) -> Result<String, CoreError> {
    init()?;
    let mut out = [0 as std::os::raw::c_char; s::crypto_pwhash_STRBYTES as usize];
    let r = unsafe {
        s::crypto_pwhash_str_alg(out.as_mut_ptr(), pw.as_ptr().cast(), pw.len() as u64, ops, mem_bytes, s::crypto_pwhash_ALG_ARGON2ID13 as i32)
    };
    if r != 0 { return Err(CoreError::Hash); }
    let c = unsafe { std::ffi::CStr::from_ptr(out.as_ptr()) };
    c.to_str().map(str::to_owned).map_err(|_| CoreError::Hash)
}

fn cstr(h: &str) -> Option<std::ffi::CString> {
    if h.len() >= s::crypto_pwhash_STRBYTES as usize { return None; }
    std::ffi::CString::new(h).ok()
}

pub fn pwhash_str_verify(h: &str, pw: &[u8]) -> bool {
    if init().is_err() { return false; }
    let Some(c) = cstr(h) else { return false };
    unsafe { s::crypto_pwhash_str_verify(c.as_ptr(), pw.as_ptr().cast(), pw.len() as u64) == 0 }
}

/// 0 = parametry odpovídají, 1 = nutný rehash, -1 = neplatný řetězec
pub fn pwhash_str_needs_rehash(h: &str, ops: u64, mem_bytes: usize) -> i32 {
    if init().is_err() { return -1; }
    let Some(c) = cstr(h) else { return -1 };
    unsafe { s::crypto_pwhash_str_needs_rehash(c.as_ptr(), ops, mem_bytes) }
}
