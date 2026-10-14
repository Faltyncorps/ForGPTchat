//! Ed25519 (ed25519-dalek, strict verify) – podepisuje se vždy jen orámovaný vstup.
use crate::{framing, CoreError};
use ed25519_dalek::{Signature, Signer, SigningKey, VerifyingKey};
use zeroize::Zeroizing;

pub fn generate_secret() -> Result<Zeroizing<[u8; 32]>, CoreError> {
    let mut s = Zeroizing::new([0u8; 32]);
    getrandom::getrandom(&mut s[..]).map_err(|_| CoreError::Rng)?;
    Ok(s)
}

pub fn public_key(secret: &[u8]) -> Result<[u8; 32], CoreError> {
    Ok(key(secret)?.verifying_key().to_bytes())
}

pub fn sign_framed(secret: &[u8], msg: &[u8]) -> Result<[u8; 64], CoreError> {
    let framed = framing::frame(msg)?;
    Ok(key(secret)?.sign(&framed).to_bytes())
}

pub fn verify_framed(public: &[u8], msg: &[u8], sig: &[u8]) -> Result<bool, CoreError> {
    let pk: [u8; 32] = public.try_into().map_err(|_| CoreError::InvalidLength)?;
    let sig: [u8; 64] = sig.try_into().map_err(|_| CoreError::InvalidLength)?;
    let vk = VerifyingKey::from_bytes(&pk).map_err(|_| CoreError::InvalidKey)?;
    let framed = framing::frame(msg)?;
    Ok(vk.verify_strict(&framed, &Signature::from_bytes(&sig)).is_ok())
}

fn key(secret: &[u8]) -> Result<SigningKey, CoreError> {
    let s: Zeroizing<[u8; 32]> = Zeroizing::new(secret.try_into().map_err(|_| CoreError::InvalidLength)?);
    Ok(SigningKey::from_bytes(&s)) // SigningKey se při dropu nuluje (feature zeroize)
}
