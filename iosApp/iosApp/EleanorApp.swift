import SwiftUI
import Shared

@main
struct EleanorApp: App {
    @StateObject private var model = WalletModel()
    var body: some Scene {
        WindowGroup { RootView().environmentObject(model) }
    }
}
