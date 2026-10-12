# Váhy bezpečnostních pravidel

Skóre incidentu = `max(základní úroveň, round(Σ váha × signál))`, omezeno na 1–5. Pevné kategorie (GDPR, průnik,
nelegalita, ohrožení prostředků) vždy → úroveň 5 / Ivo. Implementace: `assistant/approval.py`.

| Kontrola / signál | Váha | Obranná akce |
|---|---|---|
| Únik seedu / klíče (podezření) | 5.0 | zmrazit převody, notifikovat Iva, doporučit přesun prostředků |
| Únik osobních údajů (GDPR) | 5.0 | izolovat, zachovat důkazy, Ivo → hlášení ÚOOÚ do 72 h |
| Neautorizovaný přístup k backendu/repo | 4.5 | zneplatnit tokeny, rotovat klíče, logy |
| Únik API tokenu / feed URL | 4.0 | revokovat token, audit |
| Phishing / podvodná doména cílící uživatele | 3.5 | varování v aplikaci, report registrátorovi |
| Selhání biometrie opakovaně (≥ 5) | 2.5 | dočasný zámek, vyžádat PIN zařízení |
| Mainnet adresa v testnet režimu | 1.5 | blokovat, vysvětlit |
| Neplatná adresa / chybný vstup | 0.5 | validace, nápověda |
| Výpadek veřejného API | 0.5 | fallback, zobrazit stav |

## Kontroly v aplikaci (váha = priorita implementace)
| Kontrola | Váha | Stav |
|---|---|---|
| Seed jen v Keystore/Keychain, vázán na biometrii | 5 | ✅ |
| Podpis jen na testnetu (mainnet zamčen) | 5 | ✅ |
| Žádné klíče v PWA / u AI | 5 | ✅ |
| Žádné tajemství v repozitáři (.gitignore, kontrola) | 4 | ✅ |
| Validace adres + náhled poplatku před odesláním | 4 | ✅ |
| Audit log incidentů (hash řetězec) | 3 | stub |
| Rate-limit / zámek po selhání autentizace | 3 | systémový (BiometricPrompt) |
