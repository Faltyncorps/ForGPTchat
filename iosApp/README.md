# Eleanor iOS (standalone, testnet)

SwiftUI app using the Kotlin Multiplatform `Shared` framework (wallet core, BIP84, BIP21, fee preview, signing,
mempool.space history + network info). Screens: Příjem (QR), Převod (QR scan + Face ID confirm), Historie, Síť.

- Seed: Keychain item with `biometryCurrentSet` access control (Face ID / Touch ID), this device only.
- Network: **testnet only**, direct mempool.space (no RunPod / ELFALL network). Mainnet data on the Síť tab is display-only.

## Build (macOS)
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
brew install xcodegen && cd iosApp && xcodegen generate
xcodebuild -project Eleanor.xcodeproj -scheme Eleanor -sdk iphonesimulator -arch arm64 \
  -configuration Debug CODE_SIGNING_ALLOWED=NO build
```
CI (`.github/workflows/ci.yml`, job `ios-simulator`) does this and uploads `Eleanor-simulator.app.zip`.

## Real iPhone
Installing on a physical iPhone / TestFlight requires an **Apple Developer Program** membership (99 USD/yr),
signing certificates and a provisioning profile. Not set up — the CI artifact runs only in the iOS Simulator.
