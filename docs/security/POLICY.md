# Bezpečnostní politika projektu ELFALL / Fallcoin / Eleanor

Platí pro všechny přispěvatele i automatizované asistenty. Vlastník: GitHub `@ivosicek123`.

1. **Pouze obrana.** Žádné útočné nástroje, žádné testy proti cizím systémům bez písemného svolení.
2. **Žádná tajemství v repozitáři** (klíče, seedy, client_secret, tokeny, čísla účtů, osobní údaje). Konfigurace jen přes proměnné prostředí; vzory jen `*.env.example`. Kontrola gitleaks + integrity CI.
3. **Žádný merge, force-push ani přepis historie** bez výslovného „ano“ vlastníka. Staré větve se nemažou.
4. **Soukromé, dokud vlastník projekt neuvolní.** Žádné veřejné nasazení ani veřejné forky.
5. **Off-ramp pouze MoonPay a Coinbase** (URL prefixy pinované v `integrity/critical.json`).
6. **yFLC je zmrazen.** **FLC: cílová/referenční hodnota 0,33 USD** – pouze zobrazovací, nejde o garantovaný peg.
7. **Soulad s MiCA** (vč. 14denní lhůty pro odstoupení dle čl. 13) a AML; mainnet jen po právním posouzení.
8. **Ověření identity: Bank iD** (OIDC Authorization Code + PKCE S256, loa3). Klient nikdy nedrží client_secret.
9. **Kryptografická politika:** id_token PS512 (vyžaduje Bank iD) / PS256 / ES256; RS256, HS*, `none` zakázány. Integrity manifest SHA-512, podpisy jen Ed25519 (ideálně FIDO2 `sk-ssh-ed25519`). AEAD (AES-256-GCM / ChaCha20-Poly1305) pro data v klidu; klíče v Android Keystore / iOS Secure Enclave.
10. **Supply chain:** akce pinované na SHA, Gradle dependency verification (SHA-512), pip `--require-hashes`, Dependabot s cooldownem, SBOM, Scorecard, CodeQL.
11. **Review:** změny v `CODEOWNERS` cestách vyžadují review vlastníka; změny chráněných souborů vyžadují nový podpis manifestu.
12. **Incidenty** se zapisují do `docs/security/SECURITY-LOG.md` (bez osobních údajů).
