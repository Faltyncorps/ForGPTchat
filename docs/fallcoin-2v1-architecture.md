# Fallcoin 2-in-1 architecture

**Brand:** Fallcoin (bot renamed Fallcoin). Product umbrella over Eleanor (self-custodial Bitcoin wallet) and a RunPod serverless synthesis layer (read-only chain data + AI explainer).

**Legal boundary (hard):** Fallcoin only operates on **your own** keys, addresses and watchlists. No cracking Chrome/password vaults, no mining other people's sites or devices, no decoding other people's crypto products without permission, no holding or signing with third-party keys. The AI assistant **explains** balances, fees and addresses; it **never** signs transactions or stores seeds.

---

## Track A — Eleanor (existing PR #4)

Canonical code lives in `ivosicek123/bitcoin-wallet-android` branch `eleanor-rebuild` → PR #4.

| Piece | Status |
|---|---|
| KMP `shared/` (BIP39/BIP84 via bitcoin-kmp, AddressValidator, EsploraClient, WithdrawService) | Done in PR #4 |
| Android Compose app, Keystore seed, biometric send gate | Done (testnet APK builds, 7/7 unit tests) |
| iOS SwiftUI skeleton | Skeleton only (needs macOS/Xcode) |
| "Withdraw to my crypto wallet" (external BTC address + fee preview) | Done; bank/CZK flow removed |
| Default network | Testnet (`bitcoinTestnet` flavor); mainnet opt-in |
| CI | GitHub Actions (Android + non-blocking iOS framework) |

**Next on Track A (not this commit):** biometric-bound Keystore (`setUserAuthenticationRequired`), address scanning, QR, iOS Keychain + Face ID, security review before mainnet.

---

## Track B — RunPod serverless synthesis ("second railway")

Deploy as **RunPod Serverless** (Python FastAPI or Flask). RunPod balance is **$0 today**, so this starts as local Docker + a stub; scale when credit is available. Auth: a bearer token stored as a RunPod secret (`FALLCOIN_API_TOKEN`); never commit it.

### Endpoints (read-only)

| Path | Role | Upstream |
|---|---|---|
| `GET /v1/btc/address/{addr}` | Balance, UTXOs, recent txs | mempool.space / Esplora (network-scoped) |
| `GET /v1/btc/fees` | Recommended sat/vB | mempool.space |
| `POST /v1/btc/broadcast` | Relay **already-signed** hex (optional; reject if not valid hex) | mempool.space |
| `GET /v1/xlm/account/{addr}` | Stellar balances, ledger | horizon.stellar.org |
| `GET /v1/prices?ids=bitcoin,stellar&vs=czk,usd` | Multi-asset spot prices | CoinGecko (public) |
| `GET /v1/watch/{mode}` | Mode presets: `btc-testnet`, `btc-mainnet`, `xlm`, `multi` | composed of above |
| `POST /v1/assistant` | AI explainer: balances, fees, address formats, how-tos | LLM API (config later) |

### AI assistant rules (enforced in code)

- Input: address, fee quote, balance snapshot, user question.
- Allowed: explain what an address is, why a fee is high, how RBF works, how to read a Stellar balance.
- **Forbidden:** ask for or accept seeds/private keys/passphrases; sign or construct spendable txs; decode Chrome vault dumps; scrape or mine third-party sites/devices; claim ownership of third-party issuer accounts (e.g. yXLM issuer).

### Modes (shared with the app)

1. **Eleanor Wallet** — Track A on-device; talks to Track B only for fees/prices (optional; can talk to mempool.space directly).
2. **Watch** — xpub / address / Stellar account only; no keys on the phone for that mode.
3. **Markets** — multi-asset price board (from the Grok web app's CoinGecko list, rewritten).
4. **Assistant** — chat UI bound to `/v1/assistant`.

### Proposed repo layout (new or under `Falt-n-`)

```
fallcoin-server/
  app/main.py          # FastAPI
  app/btc.py           # Esplora proxy
  app/stellar.py       # Horizon read-only
  app/prices.py        # CoinGecko
  app/assistant.py     # guarded LLM
  Dockerfile
  runpod_handler.py    # serverless entry
  README.md
```

Reuse from `/workspace/grok-app/`: Stellar StrKey validation and Horizon fetch ideas (`stellar.ts`), markets list (`markets.ts`), withdrawal limits as **display** constants — **not** `authorization.ts` (bank number) or owner PII.

---

## Merging into one Fallcoin app

```
                 +-------------------------------+
                 |   Fallcoin shell (branding)   |
                 | modes: Wallet | Watch |       |
                 |        Markets | Assistant    |
                 +---------------+---------------+
                                 |
             +-------------------+-------------------+
             v                   v                   v
   Track A (on-device)    Track B (RunPod)       Docs / CI
   seed + sign           read-only APIs         this file
```

- **Shared branding:** Fallcoin name, clover logos (from attachments), ELEANOR as the wallet product line.
- **Shared types:** address strings, fee preview DTO, price quote DTO — mirrored in KMP `shared/` and the Python OpenAPI schema.
- **Key rule:** Track B never receives a seed. Track A never sends one. Broadcast endpoint accepts only signed hex the user already confirmed on-device.
- **Enterprise:** GitHub Enterprise **AgrazZzePavok** (Owner: ivosicek123) is the org home for private Fallcoin repos once OAuth grants are fixed.

---

## Cost note

- RunPod balance: **$0** as of this write-up. Develop Track B locally (`docker run` / `uvicorn`) and wire the Android app to public Esplora/Horizon directly until credit exists.
- GitHub Enterprise trial: license CSV shows an active Enterprise license for `ivosicek123` (Owner of AgrazZzePavok). Two-factor auth is **not** enabled; required-by date **2026-11-24** — enable 2FA before that.

---

## Legal boundaries (checklist)

| Allowed | Not allowed |
|---|---|
| Your BIP39 wallet, your watch addresses | Cracking Chrome/password/trusted-vault dumps |
| Public Esplora, Horizon, CoinGecko | Mining or scraping other people's sites/devices |
| Explaining public chain data with AI | Decoding others' crypto products without permission |
| Signing txs **on your device** | Server-side keys or "auth by bank account number" |
| Publishing your own ORCID / public profile facts | Committing private feed tokens, passwords, PII dumps |
