# ELFALL – Changelog

## 0.3.0 – ELFALL product rename + card cash-out (unreleased)
### Changed
- Product branding: **ELFALL** (Eleanor + Fallcoin). Package `cz.faltyn.elfall`.
- Cash-out path: **Withdraw to card** only (CZK / EUR / USD) via hosted provider stubs (MoonPay Sell, Transak, Ramp). Card details/KYC never touch the app.
- Former "withdraw to my BTC address" is now **Transfer** (crypto→crypto send).

### Added
- `shared/.../offramp` (`OffRampProvider`, `FiatCurrency`, stub providers, validation).
- `shared/.../swap` (Changelly widget stub, THORChain docs stub).
- `TransferService` façade over the on-chain send engine.
- Android UI modes: Receive / Transfer / Swap / Withdraw to card.
- Unit tests for fiat currency enum and quote validation.
- `docs/elfall-architecture.md` (bilingual).


## 0.2.0 – elfall-rebuild (unreleased)
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
- Initial ELFALL code drop (incomplete: missing build files, manifest and several referenced classes; did not compile).
