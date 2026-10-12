import SwiftUI
import Shared  // KMP framework produced by :shared (linkDebugFrameworkIosSimulatorArm64)

struct ContentView: View {
    @State private var address = ""
    @State private var result = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("ELEANOR Bitcoin Wallet").font(.title2)
            Text("Network: \(BtcNetwork.companion.DEFAULT.name)").font(.caption)
            Text("Withdraw to my crypto wallet").font(.headline)
            TextField("Destination BTC address", text: $address)
                .textFieldStyle(.roundedBorder)
                .autocorrectionDisabled()
            Button("Validate") {
                let ok = AddressValidator.shared.isValid(address: address, network: BtcNetwork.companion.DEFAULT)
                result = ok ? "Valid address" : "Invalid address"
            }
            Text(result)
            Spacer()
        }
        .padding()
        // TODO: Keychain-backed SeedStore + Face ID gate, balance, fee preview and send — mirroring androidApp.
    }
}
