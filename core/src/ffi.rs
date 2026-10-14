//! UniFFI rozhraní pro Kotlin (Android/JVM) a Swift (iOS). Tajemství se předávají jako bajty;
//! na straně Rustu se po použití nulují. Datový klíč vaultu drží platforma zabalený
//! (Android Keystore / iOS Keychain) – jádro klíče neukládá.
use crate::{framing, pin, sign, vault, CoreError};

#[uniffi::export]
pub fn signing_input(msg: Vec<u8>) -> Result<Vec<u8>, CoreError> { framing::frame(&msg) }

#[uniffi::export]
pub fn parse_signing_input(input: Vec<u8>) -> Result<Vec<u8>, CoreError> { framing::unframe(&input).map(<[u8]>::to_vec) }

#[uniffi::export]
pub fn ed25519_generate() -> Result<Vec<u8>, CoreError> { Ok(sign::generate_secret()?.to_vec()) }

#[uniffi::export]
pub fn ed25519_public_key(secret: Vec<u8>) -> Result<Vec<u8>, CoreError> {
    let s = zeroize::Zeroizing::new(secret);
    Ok(sign::public_key(&s)?.to_vec())
}

#[uniffi::export]
pub fn ed25519_sign_framed(secret: Vec<u8>, msg: Vec<u8>) -> Result<Vec<u8>, CoreError> {
    let s = zeroize::Zeroizing::new(secret);
    Ok(sign::sign_framed(&s, &msg)?.to_vec())
}

#[uniffi::export]
pub fn ed25519_verify_framed(public: Vec<u8>, msg: Vec<u8>, sig: Vec<u8>) -> Result<bool, CoreError> {
    sign::verify_framed(&public, &msg, &sig)
}

#[uniffi::export]
pub fn vault_new_key() -> Result<Vec<u8>, CoreError> { Ok(vault::new_key()?.to_vec()) }

#[uniffi::export]
pub fn vault_seal(key: Vec<u8>, field: String, plaintext: String) -> Result<String, CoreError> {
    let k = zeroize::Zeroizing::new(key);
    vault::seal(&k, &field, &plaintext)
}

#[uniffi::export]
pub fn vault_open(key: Vec<u8>, field: String, sealed: String) -> Result<String, CoreError> {
    let k = zeroize::Zeroizing::new(key);
    Ok(vault::open(&k, &field, &sealed)?.to_string())
}

#[uniffi::export]
pub fn pin_hash(pin: String) -> Result<String, CoreError> { let p = zeroize::Zeroizing::new(pin); pin::hash(&p) }

#[uniffi::export]
pub fn pin_verify(pin: String, phc: String) -> bool { let p = zeroize::Zeroizing::new(pin); pin::verify(&p, &phc) }

#[uniffi::export]
pub fn pin_needs_rehash(phc: String) -> bool { pin::needs_rehash(&phc) }
