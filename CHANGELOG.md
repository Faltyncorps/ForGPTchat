# ELEANOR Bitcoin Wallet – Changelog

## 0.2.0 – eleanor-rebuild (unreleased)
### Removed
- Bank / CZK withdrawal flow (Partners banka / Anycoin M.P.), the hard-coded bank account, and the
  `banking_api_key` / `banking_api_secret` credentials.
- Plaintext private-key export (`privateKeyAsHex`, `retrievePrivateKey`).
- Stray `Ivosicek123` file (it was this changelog, accidentally renamed in f195228).
- Third-party API keys (`blockchain_api_key`, `blockcypher_token`) – public Esplora endpoints need none.

### Added
- Kotlin Multiplatform layout: `shared` (wallet logic, Android + iOS), `androidApp` (Jetpack Compose), `iosApp` (SwiftUI skeleton).
- BIP39 12-word mnemonic + BIP84 native SegWit wallet via `bitcoin-kmp`; mnemonic encrypted with an Android Keystore AES-GCM key.
- "Withdraw to my crypto wallet": send BTC to a user-entered or saved external address with address validation,
  fee preview (sat/vB, vbytes, change) and biometric / device-credential confirmation before signing.
- Testnet is the default network; mainnet only via the `bitcoinMainnet` build flavor.
- Gradle wrapper and build files, AndroidManifest, unit tests (BIP84 test vector, validation, coin selection, signing), GitHub Actions CI.

## 1.0.0 – 2026-09-02 (original skeleton)
- Initial ELEANOR code drop (incomplete: missing build files, manifest and several referenced classes; did not compile).
