import SwiftUI
import Shared

@main
struct EleanorApp: App {
    @StateObject private var model = WalletModel()
    init() {
        // Bank iD OIDC (ASWebAuthenticationSession) + Secure Enclave klíč – registrace do KMP shared.
        IosBridge.shared.register(session: BankIdAuthSession(), signer: SecureEnclaveSigner())
    }
    var body: some Scene {
        WindowGroup { RootView().environmentObject(model) }
    }
}
