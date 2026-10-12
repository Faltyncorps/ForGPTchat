# iosApp (SwiftUI skeleton)

The iOS app consumes the Kotlin Multiplatform `:shared` module (wallet core, address validation, fee
preview, signing) as the `Shared` framework. Building requires macOS + Xcode 15+.

1. `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
2. In Xcode create an iOS App project named `iosApp` in this folder (SwiftUI lifecycle), add the two Swift
   files here, and add a Run Script build phase before "Compile Sources":
   `cd "$SRCROOT/.." && ./gradlew :shared:embedAndSignAppleFrameworkForXcode`
3. Set `FRAMEWORK_SEARCH_PATHS` to `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.

Status: skeleton only (address validation screen). The `.xcodeproj` is not committed yet because it
could not be generated/verified on the Linux build box. Keychain `SeedStore` and Face ID gating are TODO.
