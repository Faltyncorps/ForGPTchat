# elfall-core (Rust) – fáze 2 ZERO-TRUST-ROADMAP

Jedna implementace kryptografie pro Android/iOS (UniFFI, feature `ffi`) a web (wasm-bindgen, feature `wasm`).
Prověřené crates: RustCrypto `chacha20poly1305` (audit NCC), `argon2`, `ed25519-dalek` 2 (strict verify), `zeroize`.
`#![forbid(unsafe_code)]`.

| Modul | Stav | Kompatibilita |
|---|---|---|
| `framing` – vstup podpisu `"ELFALL-SIG-v1\0" ‖ len_be32 ‖ msg` | **první migrovaný modul** | přesně specifikace dokázaná ve SPARK (`proofs/spark/`) |
| `sign` – Ed25519 nad orámovaným vstupem | hotovo | RFC 8032 vektor |
| `vault` – XChaCha20-Poly1305, `sv1:` formát | hotovo, zatím nepoužito v aplikacích | vektor ověřen libsodium (Android test `rustCoreVaultVector`) |
| `pin` – Argon2id m=19456,t=2,p=1 | hotovo, zatím nepoužito | PHC ověřen libsodium (`rustCoreArgon2idVector`) |

Web dostává jen veřejné operace (rámování, ověření podpisu) – tajné klíče v prohlížeči nedržíme.
Vygenerované vazby: `bindings/` (Kotlin `uniffi/elfall_core/`, Swift `elfall_core.swift`).
