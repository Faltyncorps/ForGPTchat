# Codebase compare: Android ↔ iOS (jscpd 5 `--compare`)

*Vygenerováno 14. 10. 2026 nástrojem jscpd 5 (sémantický model nomic-ai/CodeRankEmbed, lokálně). Zdroje: `androidApp/src` vs `iosApp`, ignorováno build/target/.gradle/uniffi/Generated*. Report neobsahuje žádná tajemství – jen názvy funkcí a cesty.*

## Shrnutí

| Běh | Android funkcí s protějškem | iOS funkcí s protějškem |
|---|--:|--:|
| A) vč. vygenerovaných UniFFI vazeb `iosApp/RustCore` | 9 / 27 = **33.3 %** | 9 / 115 = **7.8 %** |
| B) bez `iosApp/RustCore` (jen ručně psaný kód) | 8 / 27 = **29.6 %** | 6 / 66 = **9.1 %** |

- Testy: 3/7 Android testů má protějšek na iOS (43 %), 3/5 iOS testů má protějšek na Androidu (60 %).
- Nejsilnější páry: `SodiumVault.open` 0.86, `SodiumVault.seal` 0.82 (vysoká shoda), `WalletViewModel.create/restore` ↔ `WalletModel` 0.63–0.67.
- Klasická detekce klonů (androidApp, iosApp, shared, core, web-leptos, backend): **0.35 %** duplicit (4 klony, 27 / 7782 řádků).
- Výklad: nízká shoda je očekávaná – Android = Compose + Keystore, iOS = UIKit + Metal + Secure Enclave; sdílená logika je v Rust core (UniFFI), ne kopírovaná mezi platformami.

## Plný report (běh A)

### `androidApp/src` compared with `iosApp`

## Code

| Side | Functions | With a counterpart | % |
|---|--:|--:|--:|
| `androidApp/src` | 27 | 9 | 33.33 |
| `iosApp` | 115 | 9 | 7.83 |

### `androidApp/src`

| File | With a counterpart | Similarity | Counterpart file |
|---|--:|--:|---|
| `main/kotlin/cz/faltyn/elfall/android/BiometricGate.kt` | 0 / 1 |  |  |
| `main/kotlin/cz/faltyn/elfall/android/KeystoreKeyWrapper.kt` | 0 / 3 |  |  |
| `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt` | 0 / 5 |  |  |
| `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt` | 3 / 9 | 0.50, 3 low | `iosApp/Controllers.swift` |
| `main/kotlin/cz/faltyn/elfall/android/QrCode.kt` | 0 / 1 |  |  |
| `main/kotlin/cz/faltyn/elfall/android/RustCore.kt` | 0 / 1 |  |  |
| `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt` | 4 / 4 | 0.73, 1 low | `RustCore/elfall_core.swift` |
| `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt` | 2 / 3 | 0.65 | `iosApp/WalletModel.swift` |

### `iosApp`

| File | With a counterpart | Similarity | Counterpart file |
|---|--:|--:|---|
| `RustCore/elfall_core.swift` | 2 / 49 | 0.62, 1 low | `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt` |
| `iosApp/AppDelegate.swift` | 0 / 2 |  |  |
| `iosApp/BankIdAuthSession.swift` | 0 / 1 |  |  |
| `iosApp/ChartMath.swift` | 0 / 5 |  |  |
| `iosApp/Controllers.swift` | 2 / 15 | 0.53, 2 low | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt` |
| `iosApp/KeychainSeedStore.swift` | 0 / 3 |  |  |
| `iosApp/MetalViews.swift` | 1 / 16 | 0.44, 1 low | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt` |
| `iosApp/QRScannerViewController.swift` | 0 / 1 |  |  |
| `iosApp/RustGpuHandoff.swift` | 0 / 4 |  |  |
| `iosApp/SecureEnclaveSigner.swift` | 0 / 5 |  |  |
| `iosApp/SecureRenderFlow.swift` | 0 / 2 |  |  |
| `iosApp/SodiumVault.swift` | 2 / 3 | 0.84 | `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt` |
| `iosApp/Transitions.swift` | 0 / 1 |  |  |
| `iosApp/WalletModel.swift` | 2 / 8 | 0.65 | `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt` |

### Only in `androidApp/src` (18)

| Function | Place | Lines |
|---|---|--:|
| `confirm` | `main/kotlin/cz/faltyn/elfall/android/BiometricGate.kt:17` | 14 |
| `key` | `main/kotlin/cz/faltyn/elfall/android/KeystoreKeyWrapper.kt:16` | 12 |
| `gen` | `main/kotlin/cz/faltyn/elfall/android/KeystoreKeyWrapper.kt:19` | 6 |
| `unwrap` | `main/kotlin/cz/faltyn/elfall/android/KeystoreKeyWrapper.kt:34` | 5 |
| `key` | `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt:30` | 9 |
| `generate` | `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt:40` | 22 |
| `saveMnemonic` | `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt:65` | 8 |
| `loadMnemonic` | `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt:74` | 7 |
| `getSealed` | `main/kotlin/cz/faltyn/elfall/android/KeystoreSeedStore.kt:105` | 5 |
| `AppScreen` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:78` | 136 |
| `HistoryRow` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:236` | 10 |
| `label` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:248` | 10 |
| `HomeGrid` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:268` | 31 |
| `BalanceCard` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:301` | 11 |
| `EmptyState` | `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:314` | 8 |
| `bitmap` | `main/kotlin/cz/faltyn/elfall/android/QrCode.kt:10` | 6 |
| `frameKotlin` | `main/kotlin/cz/faltyn/elfall/android/RustCore.kt:40` | 5 |
| `verifyFlcIssuer` | `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt:87` | 12 |

### Only in `iosApp` (106)

| Function | Place | Lines |
|---|---|--:|
| `init` | `RustCore/elfall_core.swift:17` | 6 |
| `init` | `RustCore/elfall_core.swift:53` | 7 |
| `readInt` | `RustCore/elfall_core.swift:83` | 15 |
| `readBytes` | `RustCore/elfall_core.swift:101` | 12 |
| `lift` | `RustCore/elfall_core.swift:197` | 9 |
| `lower` | `RustCore/elfall_core.swift:210` | 5 |
| `withLock` | `RustCore/elfall_core.swift:245` | 5 |
| `init` | `RustCore/elfall_core.swift:258` | 10 |
| `rustCallWithError` | `RustCore/elfall_core.swift:275` | 5 |
| `makeRustCall` | `RustCore/elfall_core.swift:281` | 10 |
| `uniffiCheckCallStatus` | `RustCore/elfall_core.swift:292` | 34 |
| `uniffiTraitInterfaceCall` | `RustCore/elfall_core.swift:327` | 12 |
| `uniffiTraitInterfaceCallWithError` | `RustCore/elfall_core.swift:340` | 16 |
| `insert` | `RustCore/elfall_core.swift:362` | 8 |
| `get` | `RustCore/elfall_core.swift:371` | 8 |
| `remove` | `RustCore/elfall_core.swift:381` | 8 |
| `lift` | `RustCore/elfall_core.swift:432` | 10 |
| `lower` | `RustCore/elfall_core.swift:443` | 10 |
| `write` | `RustCore/elfall_core.swift:459` | 5 |
| `write` | `RustCore/elfall_core.swift:477` | 5 |
| `client` | `RustCore/elfall_core.swift:558` | 7 |
| `server` | `RustCore/elfall_core.swift:569` | 8 |
| `acceptHeader` | `RustCore/elfall_core.swift:580` | 6 |
| `binding` | `RustCore/elfall_core.swift:590` | 6 |
| `header` | `RustCore/elfall_core.swift:597` | 6 |
| `` `open` `` | `RustCore/elfall_core.swift:604` | 7 |
| `publicKey` | `RustCore/elfall_core.swift:612` | 6 |
| `seal` | `RustCore/elfall_core.swift:619` | 8 |
| `read` | `RustCore/elfall_core.swift:648` | 10 |
| `write` | `RustCore/elfall_core.swift:659` | 5 |
| `==` | `RustCore/elfall_core.swift:702` | 9 |
| `read` | `RustCore/elfall_core.swift:724` | 7 |
| `==` | `RustCore/elfall_core.swift:772` | 9 |
| `read` | `RustCore/elfall_core.swift:794` | 7 |
| `read` | `RustCore/elfall_core.swift:844` | 18 |
| `write` | `RustCore/elfall_core.swift:863` | 36 |
| `ed25519Generate` | `RustCore/elfall_core.swift:936` | 6 |
| `ed25519PublicKey` | `RustCore/elfall_core.swift:942` | 7 |
| `ed25519SignFramed` | `RustCore/elfall_core.swift:949` | 8 |
| `ed25519VerifyFramed` | `RustCore/elfall_core.swift:957` | 9 |
| `parseSigningInput` | `RustCore/elfall_core.swift:966` | 7 |
| `pinNeedsRehash` | `RustCore/elfall_core.swift:980` | 7 |
| `signingInput` | `RustCore/elfall_core.swift:995` | 7 |
| `vaultNewKey` | `RustCore/elfall_core.swift:1002` | 6 |
| `vaultOpen` | `RustCore/elfall_core.swift:1008` | 9 |
| `vaultSeal` | `RustCore/elfall_core.swift:1017` | 9 |
| `uniffiEnsureElfallCoreInitialized` | `RustCore/elfall_core.swift:1111` | 10 |
| `application` | `iosApp/AppDelegate.swift:13` | 6 |
| `scene` | `iosApp/AppDelegate.swift:25` | 7 |
| `authenticate` | `iosApp/BankIdAuthSession.swift:9` | 14 |
| `resample` | `iosApp/ChartMath.swift:6` | 8 |
| `fit` | `iosApp/ChartMath.swift:16` | 7 |
| `ortho` | `iosApp/ChartMath.swift:24` | 7 |
| `mix` | `iosApp/ChartMath.swift:37` | 5 |
| `rustPointCount` | `iosApp/ChartMath.swift:47` | 5 |
| `viewDidLoad` | `iosApp/Controllers.swift:14` | 6 |
| `show` | `iosApp/Controllers.swift:21` | 10 |
| `alert` | `iosApp/Controllers.swift:32` | 5 |
| `init` | `iosApp/Controllers.swift:76` | 9 |
| `viewDidLoad` | `iosApp/Controllers.swift:99` | 21 |
| `render` | `iosApp/Controllers.swift:121` | 5 |
| `viewDidLoad` | `iosApp/Controllers.swift:139` | 21 |
| `validate` | `iosApp/Controllers.swift:161` | 5 |
| `scan` | `iosApp/Controllers.swift:167` | 14 |
| `viewDidLoad` | `iosApp/Controllers.swift:192` | 27 |
| `viewWillAppear` | `iosApp/Controllers.swift:222` | 7 |
| `tableView` | `iosApp/Controllers.swift:231` | 8 |
| `pin` | `iosApp/Controllers.swift:285` | 15 |
| `hasSeed` | `iosApp/KeychainSeedStore.swift:11` | 7 |
| `save` | `iosApp/KeychainSeedStore.swift:19` | 13 |
| `load` | `iosApp/KeychainSeedStore.swift:34` | 13 |
| `pipeline` | `iosApp/MetalViews.swift:13` | 12 |
| `init` | `iosApp/MetalViews.swift:42` | 9 |
| `layoutSubviews` | `iosApp/MetalViews.swift:53` | 7 |
| `setNeedsRender` | `iosApp/MetalViews.swift:62` | 6 |
| `frame` | `iosApp/MetalViews.swift:70` | 13 |
| `cgImage` | `iosApp/MetalViews.swift:93` | 6 |
| `encode` | `iosApp/MetalViews.swift:112` | 14 |
| `init` | `iosApp/MetalViews.swift:135` | 5 |
| `layoutSubviews` | `iosApp/MetalViews.swift:142` | 9 |
| `encode` | `iosApp/MetalViews.swift:159` | 6 |
| `setImage` | `iosApp/MetalViews.swift:182` | 13 |
| `setSeries` | `iosApp/MetalViews.swift:217` | 10 |
| `setRustVertices` | `iosApp/MetalViews.swift:238` | 5 |
| `encodeRust` | `iosApp/MetalViews.swift:243` | 21 |
| `encode` | `iosApp/MetalViews.swift:267` | 23 |
| `viewDidLoad` | `iosApp/QRScannerViewController.swift:13` | 16 |
| `chart` | `iosApp/RustGpuHandoff.swift:32` | 9 |
| `chart` | `iosApp/RustGpuHandoff.swift:42` | 6 |
| `take` | `iosApp/RustGpuHandoff.swift:56` | 6 |
| `releaseUnconsumed` | `iosApp/RustGpuHandoff.swift:64` | 6 |
| `loadOrCreate` | `iosApp/SecureEnclaveSigner.swift:13` | 19 |
| `sign` | `iosApp/SecureEnclaveSigner.swift:42` | 10 |
| `publicKeyX963` | `iosApp/SecureEnclaveSigner.swift:53` | 5 |
| `signEs256` | `iosApp/SecureEnclaveSigner.swift:59` | 7 |
| `derToRaw` | `iosApp/SecureEnclaveSigner.swift:68` | 6 |
| `showSealedChart` | `iosApp/SecureRenderFlow.swift:12` | 31 |
| `makeNoCopyBuffer` | `iosApp/SecureRenderFlow.swift:47` | 9 |
| `dataKey` | `iosApp/SodiumVault.swift:31` | 9 |
| `crossfade` | `iosApp/Transitions.swift:6` | 17 |
| `unlock` | `iosApp/WalletModel.swift:33` | 6 |
| `refresh` | `iosApp/WalletModel.swift:44` | 6 |
| `loadHistory` | `iosApp/WalletModel.swift:51` | 6 |
| `previewTransfer` | `iosApp/WalletModel.swift:58` | 9 |
| `send` | `iosApp/WalletModel.swift:68` | 10 |
| `loadNetwork` | `iosApp/WalletModel.swift:79` | 6 |

### Paired under other names (7)

| `androidApp/src` | `iosApp` | Similarity | Level | Matched by |
|---|---|--:|---|---|
| `onCreate` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:53` | `init` `iosApp/MetalViews.swift:173` | 0.44 | low | code |
| `NetworkPanel` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:216` | `viewDidLoad` `iosApp/Controllers.swift:255` | 0.56 | low | code |
| `SettingsPanel` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:324` | `viewDidLoad` `iosApp/Controllers.swift:49` | 0.50 | low | code |
| `hash` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:56` | `pinHash` `RustCore/elfall_core.swift:973` | 0.70 | medium | code |
| `verify` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:65` | `pinVerify` `RustCore/elfall_core.swift:987` | 0.55 | low | code |
| `createWalletAfterAuth` `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt:110` | `create` `iosApp/WalletModel.swift:21` | 0.67 | medium | code |
| `restoreWalletAfterAuth` `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt:117` | `restore` `iosApp/WalletModel.swift:27` | 0.63 | medium | code |

### Pairs (9)

| `androidApp/src` | `iosApp` | Similarity | Level | Matched by |
|---|---|--:|---|---|
| `onCreate` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:53` | `init` `iosApp/MetalViews.swift:173` | 0.44 | low | code |
| `NetworkPanel` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:216` | `viewDidLoad` `iosApp/Controllers.swift:255` | 0.56 | low | code |
| `SettingsPanel` `main/kotlin/cz/faltyn/elfall/android/MainActivity.kt:324` | `viewDidLoad` `iosApp/Controllers.swift:49` | 0.50 | low | code |
| `seal` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:21` | `seal` `iosApp/SodiumVault.swift:41` | 0.82 | high | code |
| `open` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:31` | `open` `iosApp/SodiumVault.swift:49` | 0.86 | high | code |
| `hash` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:56` | `pinHash` `RustCore/elfall_core.swift:973` | 0.70 | medium | code |
| `verify` `main/kotlin/cz/faltyn/elfall/android/SodiumVault.kt:65` | `pinVerify` `RustCore/elfall_core.swift:987` | 0.55 | low | code |
| `createWalletAfterAuth` `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt:110` | `create` `iosApp/WalletModel.swift:21` | 0.67 | medium | code |
| `restoreWalletAfterAuth` `main/kotlin/cz/faltyn/elfall/android/WalletViewModel.kt:117` | `restore` `iosApp/WalletModel.swift:27` | 0.63 | medium | code |

## Tests

| Side | Tests | With a counterpart | % |
|---|--:|--:|--:|
| `androidApp/src` | 7 | 3 | 42.86 |
| `iosApp` | 5 | 3 | 60 |

### `androidApp/src`

| File | With a counterpart | Similarity | Counterpart file |
|---|--:|--:|---|
| `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt` | 3 / 5 | 0.71 | `Tests/RustCoreTests.swift` |
| `test/kotlin/cz/faltyn/elfall/android/SodiumVaultTest.kt` | 0 / 2 |  |  |

### `iosApp`

| File | With a counterpart | Similarity | Counterpart file |
|---|--:|--:|---|
| `Tests/RustCoreTests.swift` | 3 / 5 | 0.71 | `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt` |

### Only in `androidApp/src` (4)

| Function | Place | Lines |
|---|---|--:|
| `framingKotlinEqualsRust` | `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:24` | 8 |
| `argon2idInterop` | `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:53` | 7 |
| `tamperDetected` | `test/kotlin/cz/faltyn/elfall/android/SodiumVaultTest.kt:20` | 5 |
| `argon2idOwaspParams` | `test/kotlin/cz/faltyn/elfall/android/SodiumVaultTest.kt:30` | 7 |

### Only in `iosApp` (2)

| Function | Place | Lines |
|---|---|--:|
| `testUnitFitAndRustPointCount` | `Tests/RustCoreTests.swift:5` | 6 |
| `testNoCopyBufferSharesRustMemoryAndFreesOnRelease` | `Tests/RustCoreTests.swift:63` | 12 |

### Paired under other names (3)

| `androidApp/src` | `iosApp` | Similarity | Level | Matched by |
|---|---|--:|---|---|
| `rustSealsLazysodiumOpens` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:32` | `testRustSealsSodiumOpens` `Tests/RustCoreTests.swift:33` | 0.69 | medium | code |
| `lazysodiumSealsRustOpens` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:37` | `testSodiumSealsRustOpens` `Tests/RustCoreTests.swift:39` | 0.80 | high | code |
| `ed25519LibsodiumInterop` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:43` | `testEd25519AndArgon2Interop` `Tests/RustCoreTests.swift:49` | 0.65 | medium | code |

### Pairs (3)

| `androidApp/src` | `iosApp` | Similarity | Level | Matched by |
|---|---|--:|---|---|
| `rustSealsLazysodiumOpens` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:32` | `testRustSealsSodiumOpens` `Tests/RustCoreTests.swift:33` | 0.69 | medium | code |
| `lazysodiumSealsRustOpens` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:37` | `testSodiumSealsRustOpens` `Tests/RustCoreTests.swift:39` | 0.80 | high | code |
| `ed25519LibsodiumInterop` `test/kotlin/cz/faltyn/elfall/android/RustCoreTest.kt:43` | `testEd25519AndArgon2Interop` `Tests/RustCoreTests.swift:49` | 0.65 | medium | code |
