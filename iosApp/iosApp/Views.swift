import SwiftUI
import CoreImage.CIFilterBuiltins
import Shared

struct RootView: View {
    @EnvironmentObject var m: WalletModel
    var body: some View {
        NavigationStack {
            Group {
                if !m.unlocked { OnboardingView() }
                else {
                    TabView {
                        ReceiveView().tabItem { Label("Příjem", systemImage: "qrcode") }
                        TransferView().tabItem { Label("Převod", systemImage: "arrow.up.right") }
                        HistoryView().tabItem { Label("Historie", systemImage: "clock") }
                        NetworkView().tabItem { Label("Síť", systemImage: "network") }
                    }
                }
            }
            .navigationTitle("Eleanor · testnet")
            .toolbar { if m.unlocked { Button("Zamknout") { m.lock() } } }
            .alert(m.error ?? "", isPresented: Binding(get: { m.error != nil }, set: { if !$0 { m.error = nil } })) { Button("OK") {} }
        }
    }
}

struct OnboardingView: View {
    @EnvironmentObject var m: WalletModel
    @State private var phrase = ""
    var body: some View {
        Form {
            if m.hasSeed {
                Button("Odemknout (Face ID)") { m.unlock() }
            } else {
                Button("Nová peněženka / Create wallet") { m.create() }
                Section("Obnovit z 12 slov") {
                    TextField("abandon abandon …", text: $phrase, axis: .vertical).autocorrectionDisabled().textInputAutocapitalization(.never)
                    Button("Obnovit / Restore") { m.restore(phrase) }
                }
            }
            Section { NetworkView() }
        }
    }
}

struct ReceiveView: View {
    @EnvironmentObject var m: WalletModel
    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                if let w = m.newMnemonic {
                    GroupBox("Zapište si 12 slov na papír") {
                        Text(w.joined(separator: " ")).textSelection(.enabled)
                        Button("Zapsáno") { m.newMnemonic = nil }
                    }
                }
                Text("Zůstatek: \(m.balance) tBTC").font(.headline)
                Text("Adresa #\(m.addressIndex)").font(.caption)
                Text(m.address).font(.footnote.monospaced()).textSelection(.enabled)
                if let img = qr(m.facade.bip21(address: m.address)) {
                    Image(uiImage: img).interpolation(.none).resizable().frame(width: 240, height: 240)
                }
                HStack {
                    Button("Nová adresa") { m.nextAddress() }
                    Button("Obnovit") { m.refresh() }
                }
            }.padding()
        }
    }
    private func qr(_ s: String) -> UIImage? {
        let f = CIFilter.qrCodeGenerator(); f.message = Data(s.utf8)
        guard let out = f.outputImage?.transformed(by: .init(scaleX: 8, y: 8)),
              let cg = CIContext().createCGImage(out, from: out.extent) else { return nil }
        return UIImage(cgImage: cg)
    }
}

struct TransferView: View {
    @EnvironmentObject var m: WalletModel
    @State private var to = ""
    @State private var amount = ""
    @State private var scanning = false
    var body: some View {
        Form {
            Section("Převod BTC (testnet)") {
                Button("Skenovat QR") { scanning = true }
                TextField("Adresa tb1…", text: $to).autocorrectionDisabled().textInputAutocapitalization(.never)
                if !to.isEmpty, let e = m.facade.validate(address: to) { Text(e).foregroundStyle(.red).font(.caption) }
                TextField("Částka (BTC)", text: $amount).keyboardType(.decimalPad)
                Button("Náhled poplatku") { m.previewTransfer(to: to, amount: amount) }
            }
            if let p = m.preview {
                Section("Náhled") {
                    Text("Komu: \(p.destination)").font(.footnote)
                    Text("Částka: \(m.facade.formatSats(sats: p.amountSats)) BTC")
                    Text("Poplatek: \(p.feeSats) sats (\(p.feeRateSatPerVb) sat/vB)")
                    Button("Potvrdit Face ID a odeslat") { m.send() }
                }
            }
            if let t = m.lastTxid { Link("Odesláno: \(t.prefix(12))…", destination: URL(string: "https://mempool.space/testnet/tx/\(t)")!) }
        }
        .sheet(isPresented: $scanning) {
            QRScannerView { code in
                scanning = false
                if let r = m.facade.parseScanned(raw: code) {
                    let parts = r.dropFirst(4).split(separator: "|").map(String.init)
                    if r.hasPrefix("btc:") {
                        to = parts[0]
                        if parts.count > 1, parts[1] != "-", let s = Int64(parts[1]) { amount = m.facade.formatSats(sats: s) }
                    } else { m.error = "Stellar adresa — podepište v LOBSTR / Freighter (SEP-7)" }
                } else { m.error = "Neznámý QR" }
            }
        }
    }
}

struct HistoryView: View {
    @EnvironmentObject var m: WalletModel
    var body: some View {
        List(m.history, id: \.id) { h in
            Link(destination: URL(string: h.explorerUrl)!) {
                VStack(alignment: .leading) {
                    Text("\(h.netSats > 0 ? "+" : "")\(h.amountText) tBTC")
                    Text(h.confirmations > 0 ? "\(h.confirmations) potvrzení" : "nepotvrzeno").font(.caption)
                }
            }
        }
        .overlay { if m.history.isEmpty { Text("Žádné transakce") } }
        .refreshable { m.loadHistory() }
        .onAppear { m.loadHistory() }
    }
}

struct NetworkView: View {
    @EnvironmentObject var m: WalletModel
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Toggle("Mainnet data (jen zobrazení)", isOn: $m.infoMainnet).onChange(of: m.infoMainnet) { _ in m.loadNetwork() }
            if let s = m.snapshot {
                Text("Poplatky sat/vB: \(s.fees.fastestFee) · \(s.fees.halfHourFee) · \(s.fees.hourFee)")
                Text("Mempool: \(s.mempool.count) tx")
                Text("Výška bloku: \(s.tipHeight)")
                if let p = s.prices, let usd = p.usd { Text("BTC: \(Int(usd.doubleValue)) USD (mainnet trh)") }
                ForEach(s.recentBlocks, id: \.id) { b in Text("#\(b.height) · \(b.txCount) tx").font(.caption) }
            } else { ProgressView() }
        }
        .padding()
        .onAppear { if m.snapshot == nil { m.loadNetwork() } }
    }
}
