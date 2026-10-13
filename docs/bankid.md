# Bank iD (Bankovní identita) – integrace (SANDBOX)

Stav: **experimentální, feature flag vypnutý** (`IdentityFlags.bankIdEnabled = false`, iOS `BankIdFlags.enabled = false`, web `BANKID_ENABLED = false`). Jen sandbox `https://oidc.sandbox.bankid.cz/`. Produkce je v kódu zablokována (`BANKID_ENV=production` vyžaduje navíc `BANKID_ALLOW_PRODUCTION=1`).

## Architektura

- `backend/bankid/` – FastAPI služba (Python). Drží `client_secret`, PKCE verifier, nonce a state; vyměňuje code, ověřuje id_token (PS512 přes JWKS, `iss`, `aud`, `exp`, `nonce`, u transakce `auth_time`).
- `shared/.../identity/IdentityVerifier.kt` – rozhraní `IdentityVerifier`, implementace `BankIdVerifier` (volá backend), `WithdrawalGate`.
- UI: tlačítko **„Ověřit přes Bank iD“** v Nastavení (Android/iOS/web) a **„Potvrdit výběr přes Bank iD“** v kroku před výběrem/odesláním (Android/iOS). Napojení tlačítek na otevření prohlížeče (Custom Tab / ASWebAuthenticationSession) je TODO – flag je vypnutý.

Hodnoty z OIDC discovery sandboxu (`/.well-known/openid-configuration`, ověřeno 2026-10-13): `authorization_endpoint=/auth`, `token_endpoint=/token`, `profile_endpoint=/profile`, `userinfo_endpoint=/userinfo`, `jwks_uri=/.well-known/jwks`, `id_token_signing_alg=PS512`, `acr_values_supported=[loa3]`, `code_challenge_methods=[plain,S256]`, `token_endpoint_auth=client_secret_post|client_secret_jwt|private_key_jwt`.

## Tok (authorization code + PKCE)

```mermaid
sequenceDiagram
    participant U as Uživatel (app)
    participant B as ELFALL backend/bankid
    participant O as Bank iD (sandbox OIDC)
    participant Bk as Banka uživatele
    U->>B: POST /auth/start {purpose: kyc|account|txn, tx_hash?, payout_iban?}
    B->>B: state, nonce (txn: nonce=H(salt|tx_hash)), PKCE verifier/challenge S256
    B-->>U: authorize_url
    U->>O: GET /auth?response_type=code&scope=…&code_challenge=…&acr_values=loa3[&prompt=login&max_age=0]
    O->>Bk: výběr banky, přihlášení, souhlas
    O-->>U: redirect_uri?code&state
    U->>B: POST /auth/callback {code, state}
    B->>O: POST /token (code, code_verifier, client_secret_post)
    O-->>B: id_token (PS512), access_token
    B->>O: GET /.well-known/jwks → ověření podpisu, iss/aud/exp/nonce(/auth_time)
    B->>O: GET /profile (Bearer) – jen u kyc/account
    B-->>U: minimální výsledek (úroveň KYC / shoda účtu / potvrzení transakce)
```

## Účely a scopes (data minimisation)

| Účel | Scopes | Výstup backendu |
|---|---|---|
| KYC (`kyc`) | `openid profile.name profile.birthdate profile.verification` | `level` (`bank_aml_verified` = `verified_claims.verification.trust_framework` + `acr=loa3`), `majority` |
| Vlastnictví účtu (`account`) | `openid profile.name profile.paymentAccounts` | `iban_match` (IBAN výplatního účtu ∈ claim `paymentAccounts`), `name_match` (informativní), `confirmed` = shoda IBAN |
| Potvrzení transakce (`txn`) | `openid` | step-up: `prompt=login`, `max_age=0`, `acr_values=loa3`; tx hash svázán v `nonce`, kontrola `auth_time` ≤ 120 s |

Pozn.: Bank iD nabízí i scope `sign.*` a claim `txn`; pro skutečnou autorizaci transakcí se zvažuje služba **SIGN** (vyžaduje smlouvu). Zde je použit standardní step-up OIDC.

## Registrace sandboxu (úkol pro uživatele)

1. Účet na <https://developer.bankid.cz> (přihlášení, souhlas s podmínkami).
2. Vytvořit **aplikaci** v sandbox prostředí.
3. OIDC nastavení: Redirect URI (např. `https://<váš-backend>/bankid/callback` nebo app-link), grant `authorization_code`, auth metoda `client_secret_post`.
4. Scopes: povinné `openid`; volitelně `profile.name`, `profile.birthdate`, `profile.verification`, `profile.paymentAccounts` (nic navíc).
5. Zkopírovat `client_id` a `client_secret` do proměnných prostředí (`backend/bankid/.env.example` → `.env`, **necommitovat**).
6. Spuštění: `pip install -r backend/bankid/requirements.txt && uvicorn backend.bankid.app:app`; ve webu doplnit backend do CSP `connect-src`.
7. Sandbox používá testovací banku a fiktivní osoby – nikdy reálné údaje.

## Produkce – požadavky

- Uzavření **Smlouvy o poskytování služeb Bank iD (SeP – poskytovatel služeb)** s Bankovní identita, a.s. (IČO 09513817); smluvní strana je právnická osoba / podnikatel (IČO), nikoli soukromá osoba.
- Schválení aplikace, produkční client registrace, případně `private_key_jwt`, šifrování id_tokenu, backchannel logout.
- Pro využití dat k AML (IDENTIFY AML) musí být SeP povinnou osobou dle AML zákona a mít odpovídající procesy; pro krypto služby navíc licence CASP (MiCA).
- **Ceník: pro ELFALL neznámý / nesjednaný.** Veřejný ceník Bankovní identity (příloha č. 5, v10 od 1. 1. 2026) uvádí orientačně bez DPH např. CONNECT 0,49 Kč/použití nebo 10 Kč/rok, IDENTIFY 20 Kč, IDENTIFY PLUS 60 Kč, IDENTIFY AML 140 Kč (roční předplatné/uživatel), SIGN 5 Kč; plus aktivační poplatek. Ověřit aktuální verzi (2027) přímo u Bankovní identity.

## GDPR / minimalizace

- Žádat jen nezbytné scopes; `profile.birthnumber`, adresy, doklady se nežádají.
- Backend vrací klientovi jen odvozené výsledky (úroveň, true/false), ne jména, data narození ani čísla účtů; nic neperzistuje (pending state je v paměti s TTL 10 min, jednorázový).
- Právní základ: plnění smlouvy / právní povinnost (AML) – doplnit do zásad ochrany osobních údajů; logy bez osobních údajů.
- `sub` je pseudonymní identifikátor (pairwise podporováno) – ukládat jen pokud je nutné.

## Testy

- `python -m pytest -c backend/bankid/pytest.ini backend/bankid/tests` (mockovaný OIDC přes respx, RSA klíč generovaný v testu).
- `./gradlew :shared:testDebugUnitTest` (`BankIdVerifierTests`, ktor MockEngine).

## Zdroje

- <https://developer.bankid.cz/docs> (APIs, `apis_sep`, `sep_get_started`, `error-callbacks`, `java_examples`)
- <https://oidc.sandbox.bankid.cz/.well-known/openid-configuration>
- <https://bankid.cz/smluvni-dokumentace/>
- <https://bankid.cz/files/Bank-iD_SeP_Priloha-c.-5-cenik.pdf>
- OpenID Connect Core 1.0, RFC 7636 (PKCE), OpenID Connect for Identity Assurance (`verified_claims`)
