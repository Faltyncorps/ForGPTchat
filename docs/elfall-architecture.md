# ELFALL architecture / Architektura ELFALL

**CZ:** ELFALL = Eleanor + Fallcoin. Jedna 2-v-1 aplikace (jedno APK): neúschovná Bitcoin peněženka + režimy Příjem, Převod, Směna a **Výběr na kartu** (jen CZK/EUR/USD přes licencovaného poskytovatele). Kartové údaje a KYC nikdy nejdou do ELFALL.

**EN:** ELFALL = Eleanor + Fallcoin. One 2-in-1 APK: a non-custodial Bitcoin wallet plus Receive, Transfer, Swap, and **Withdraw to card** (CZK/EUR/USD only via a licensed provider's hosted widget). Card PAN/CVV and KYC never touch ELFALL.

---

## Product rules / Pravidila produktu

| Mode (EN) | Mode (CZ) | What it does | Custody |
|---|---|---|---|
| **Receive** | Příjem | Show BIP84 address / QR | Keys on device |
| **Transfer** | Převod | Send BTC to a crypto address (on-chain; later cross-network hops) | Signed on device |
| **Swap** | Směna | Crypto↔crypto via provider widget (Changelly stub / THORChain docs) | Non-custodial; ELFALL never holds funds |
| **Withdraw to card** | **Výběr na kartu** | Cash-out **only** to payment cards in **CZK, EUR or USD** | Licensed provider does KYC/AML + card processing |

**Hard rule:** "Withdrawal" / "Výběr" means **card cash-out only**. Sending BTC to another address is a **Transfer**, not a withdrawal.

**Legal:** ELFALL is non-custodial. MoonPay Sell / Transak / Ramp (stubs) perform KYC/AML and handle card data inside their hosted flows. No cracking vaults, no mining others' devices/sites, no decoding others' crypto products without permission. AI assistant (Track B) may explain balances/fees/addresses only — never sign or hold keys.

---

## Track A — on-device wallet (this repo, PR #4)

- KMP `shared/` (`cz.faltyn.elfall.shared`): WalletCore (BIP39/BIP84), AddressValidator, EsploraClient, **TransferService** (BTC send), **offramp.OffRampProvider** (card cash-out stubs), **swap.SwapProvider** (swap stubs).
- `androidApp/` (`cz.faltyn.elfall`): Compose UI with four modes; Keystore-encrypted mnemonic; biometric gate before Transfer send.
- `iosApp/`: SwiftUI skeleton.
- Default network: **testnet**. Mainnet = `bitcoinMainnet` flavor only.

### Off-ramp stubs (no real API keys)

Placeholders in `OffRampConfig`: `moonPayPublishableKey`, `transakApiKey`, `rampApiKey` = `REPLACE_ME` / `pk_test_REPLACE_ME`. Quotes are illustrative; the provider widget is authoritative. Card details never enter our process — we only open an HTTPS hosted URL.

---

## Track B — RunPod serverless (second railway)

Unchanged intent: Python endpoints for mempool.space proxy, Stellar Horizon read-only, multi-asset prices, optional AI explainer. RunPod balance **$0** today → develop locally first. Never receives seeds.

---

## Package / branding

| Item | Value |
|---|---|
| Product name | **ELFALL** |
| Android `applicationId` | `cz.faltyn.elfall` (+ `.testnet` suffix on testnet flavor) |
| Namespaces | `cz.faltyn.elfall.android`, `cz.faltyn.elfall.shared` |
| Legacy name | Eleanor / Fallcoin kept in git history and older docs only |

---

## Cost / Náklady

- RunPod: $0 until credit is added.
- Off-ramp / swap provider fees: charged by the provider inside their widget; ELFALL does not add a cash-out fee beyond showing an estimate.
