# Bezpečnostní deník (chronologicky, bez osobních údajů)

| Datum (UTC) | Typ | Záznam | Stav |
|---|---|---|---|
| 2026-09-23 | Inventura | Fork `Faltyncorp/bitcoin-wallet-android` (soukromý) vytvořen vlastníkem; poslední push 2026-09-23, větev FaltynIvo db7b4b6 | evidováno |
| 2026-09-23 | Inventura | `ivosicek123/gh-net` – **veřejný** fork `github/gh-net` (nesouvisí s kódem ELFALL) | čeká na smazání vlastníkem (token bez `delete_repo`) |
| 2026-10-13 | Inventura | Vzdálená větev `codespace-sturdy-waddle-4qwrvjv7w66rfj5vw` (780909d) – pozůstatek Codespace | ponechána, rozhodne vlastník |
| 2026-10-14 | Audit | Forenzní audit všech větví, reflogu, GitHub API a boxu: **žádný neznámý aktér**, žádné „webstr/amatér“, žádné deploy klíče/webhooky | uzavřeno |
| 2026-10-14 | Oprava | CI android selhávalo na Gradle dependency verification (chybějící BOM) – metadata přegenerována (SHA-512) | opraveno (kotlin-backend) |
| 2026-10-14 | Změna | Bank iD backend migrován z Python/FastAPI na Kotlin/Ktor + nimbus-jose-jwt (Python v historii) | hotovo (kotlin-backend) |
| 2026-10-14 | Politika | Algoritmy id_tokenu: PS512 (jediný podporovaný Bank iD), PS256, ES256; RS256/HS*/none zakázány | platí |
| 2026-10-14 | Integrita | Manifest SHA-512 + podpisy jen Ed25519 | hotovo |
| 2026-10-14 | Otevřené | Chybí veřejný Ed25519 klíč vlastníka v `integrity/allowed_signers` → CI krok podpisu selhává | čeká na vlastníka |
| 2026-10-14 | Prevence | Příručka obrany (DEFENSE-PLAYBOOK.md) a obrazovka „Bezpečnostní kontrola“ ve webové aplikaci | hotovo (security-review) |
| 2026-10-14 | Prevence | Domácí bezpečnostní sada tools/home-defense (inventura sítě, BLE, iPhone/MVT, router, důkazní log) | hotovo (security-review) |
| 2026-10-14 | Oprava | SR-04: šifrování výplatní adresy a Stellar účtu (libsodium XChaCha20-Poly1305, Android lazysodium, iOS swift-sodium); Argon2id PinHasher (OWASP) | hotovo (security-review) |
| 2026-10-14 | Infrastruktura | Hardened Dockerfile (distroless nonroot, digest), docker-compose (read-only, cap_drop ALL, no-new-privileges, seccomp), k8s (PSS restricted, NetworkPolicy default-deny); Firecracker zdokumentován jako budoucí volba | hotovo (security-review) |
| 2026-10-14 | Politika | Osobní údaje jen šifrovaně offline (age), mimo repo | platí |
| 2026-10-14 | Revize | Bezpečnostní revize (semgrep, gitleaks, osv-scanner, detekt, ruční): viz `REVIEW-2026-10-14.md` | opraveny SR-01, SR-02, SR-07; ostatní doporučení |
| 2026-10-14 | Formální důkaz | EasyCrypt: SodiumVault (XChaCha20-Poly1305, název pole jako AD) – integrita a vazba na pole redukována na AEAD INT-CTXT (`proofs/easycrypt/`), ověřeno lokálně + CI | hotovo (security-review) |
| 2026-10-14 | Formální důkaz | SPARK/Ada: rámování vstupu podpisu Ed25519 a nulování klíčů – absence běhových chyb, gnatprove v CI (`proofs/spark/`) | hotovo (security-review) |
| 2026-10-14 | Formální důkaz | Idris2: invarianty FLC (pevný strop nabídky, zmrazený yFLC nepřevoditelný) – model, CI (`proofs/idris/`) | hotovo (security-review) |
| 2026-10-14 | Autentizace | WebAuthn passkeys: backend webauthn4j (ES256/EdDSA, UV required, jednorázové challenge) + web klient, testy; úložiště zatím v paměti | rozpracováno (security-review) |
| 2026-10-14 | Plán | `ZERO-TRUST-ROADMAP.md`: passkeys → ZTNA → Rust jádro (UniFFI/Wasm) → Suricata/Zeek, Cilium/eBPF → HACL*/F* | platí |
| 2026-10-14 | Formální důkaz | Rocq 9.3: politika přístupu ke klíčům – autorizuje jen passkey + klíč zařízení vlastníka, odvolaný klíč nikdy (ani v budoucích stavech) (`proofs/rocq/`), CI | hotovo (zero-trust) |
| 2026-10-14 | Zero Trust | Backend: deny-by-default Ktor plugin, DPoP tokeny ≤ 5 min vázané na passkey credential + P-256 klíč zařízení, replay ochrana, revokace, auditní log s hash řetězem; 15 testů | hotovo (zero-trust), nenasazeno |
| 2026-10-14 | Návrh | `deploy/ztna/`: Cilium L7 politika, XDP filtr, Suricata pravidla – **nenasazeno** | dokumentováno |
| 2026-10-14 | Změna | Rust jádro `core/` (větev rust-core): první modul = rámování vstupu podpisu | čeká na schválení |
