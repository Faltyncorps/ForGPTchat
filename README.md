# ELEANOR – Bitcoin wallet for Android & iOS

ELEANOR is a self-custodial Bitcoin wallet built with **Kotlin Multiplatform**: one shared wallet core
targets both Android and iOS ("2 in 1").

> ⚠️ Early development. Defaults to **testnet**. Do not use with real funds until a security review is done.

## Features
- Create a new wallet (BIP39, 12 words, shown once for paper backup) or restore one
- BIP84 native SegWit (`tb1…` on testnet, `bc1…` on mainnet) receive address and balance
- **Withdraw to my crypto wallet**: send BTC to an external address you enter or save, with
  address validation, fee preview and biometric confirmation; signing happens on the device
- No API keys needed: uses public Esplora / mempool.space endpoints

## Project layout
```
shared/      KMP module: WalletCore (BIP39/BIP84), AddressValidator, EsploraClient, WithdrawService, SeedStore
androidApp/  Jetpack Compose app, Android Keystore seed storage, BiometricPrompt gate
iosApp/      SwiftUI skeleton consuming the Shared framework (see iosApp/README.md)
```

## Stack
Kotlin 2.2, AGP 8.7, Gradle 8.11, [bitcoin-kmp](https://github.com/ACINQ/bitcoin-kmp) + secp256k1-kmp,
Ktor 3, kotlinx.serialization, Jetpack Compose (Material 3), AndroidX Biometric. minSdk 26, targetSdk 35.

## Build
```bash
./gradlew :shared:testDebugUnitTest            # unit tests (incl. BIP84 test vector)
./gradlew :androidApp:assembleBitcoinTestnetDebug   # testnet debug APK
./gradlew :androidApp:assembleBitcoinMainnetDebug   # mainnet (opt-in)
```
iOS: see `iosApp/README.md` (macOS + Xcode).

## Security model
- The mnemonic is encrypted with an AES-256-GCM key held in Android Keystore and is never exported by the app.
- Transactions are built and signed locally; only the signed hex is broadcast.
- Sending requires biometric or device-credential confirmation.
- There are no bank integrations and no credentials stored in the repo.

## License
BSD 2-Clause.
