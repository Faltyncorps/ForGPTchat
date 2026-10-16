# ELFALL – obrana domácnosti na iPhonu (iOS 27 beta)

Cíl: **iPhone s iOS 27 beta** (zařízení ELFALL). Vše se dělá přímo na iPhonu – **žádný Linux/Windows/Mac runtime není potřeba**. Odškrtávejte v aplikaci Poznámky nebo v Markdown prohlížeči (např. ShellFish → Files).
Názvy položek v beta verzi se mohou mírně lišit; když položku nenajdete, použijte vyhledávání v Nastavení.

## 1. Aktualizace a beta
- [ ] Nastavení → Obecné → Aktualizace softwaru → nainstalovat poslední **beta** build; zapnout *Bezpečnostní odpovědi a systémové soubory*.
- [ ] Beta = vyšší riziko chyb: před každou betou zálohovat (iCloud nebo šifrovaná záloha).

## 2. Bezpečnostní kontrola (Safety Check)
- [ ] Nastavení → Soukromí a zabezpečení → **Bezpečnostní kontrola** → *Spravovat sdílení a přístup*.
- [ ] Projít lidi, aplikace, zařízení; odebrat cokoli neznámého.
- [ ] Při akutním ohrožení: *Nouzový reset*.

## 3. VPN a profily
- [ ] Nastavení → Obecné → **VPN a správa zařízení**: bez firemního telefonu zde nemá být žádný profil – neznámé odstranit.
- [ ] Nastavení → VPN: ponechat jen VPN, kterou jste sami nainstalovali.
- [ ] Nastavení → Obecné → Informace → Nastavení důvěry certifikátů: žádný neznámý kořenový certifikát.

## 4. Apple ID – zařízení a přihlášení
- [ ] Nastavení → [jméno] → seznam **zařízení** dole: odebrat neznámá.
- [ ] Přihlášení a zabezpečení: dvoufázové ověření zapnuto, **bezpečnostní klíče** (FIDO2), důvěryhodná čísla jen vlastní.
- [ ] Kód zámku min. 6 číslic/alfanumerický; **Ochrana ukradeného zařízení** zapnout.
- [ ] Aplikace Najít → Lidé: komu sdílíte polohu.

## 5. Režim izolace (Lockdown Mode)
- [ ] Při podezření na cílený útok: Nastavení → Soukromí a zabezpečení → **Režim izolace** → Zapnout → restart.
- [ ] Pozn.: omezí JIT v Safari/WKWebView a přílohy ve Zprávách – ELFALL web může být pomalejší; to je očekávané.

## 6. Wi‑Fi – Fing
- [ ] Nainstalovat **Fing** z App Store, povolit *Místní síť*.
- [ ] Skenovat domácí Wi‑Fi, pojmenovat známá zařízení, neznámá zapsat (MAC výrobce, čas).
- [ ] Nastavení → Wi‑Fi → (i) u sítě: *Soukromá adresa Wi‑Fi* = Rotující/Pevná dle potřeby; *Omezit sledování IP adresy* zapnout.

## 7. Router – změna hesla
- [ ] V Safari otevřít administraci routeru (adresa na štítku, typicky `192.168.0.1` / `192.168.1.1`).
- [ ] Změnit **heslo administrátora** (silné, uložit do Hesel na iPhonu).
- [ ] Změnit **heslo Wi‑Fi** (WPA3 nebo WPA2/WPA3), vypnout WPS a vzdálenou správu, aktualizovat firmware.
- [ ] Po změně znovu skenovat Fingem – neznámá zařízení musí zmizet.

## 8. Ostatní
- [ ] Soukromí → Poloha, Mikrofon, Kamera, Bluetooth, Místní síť: odebrat zbytečná oprávnění.
- [ ] Bluetooth: odebrat neznámá zařízení (iOS hlásí cizí AirTag).
- [ ] Operátor: zákaznický PIN, zákaz výměny SIM bez osobního ověření.

Hlubší forenzní kontrola (MVT) vyžaduje počítač – viz `../iphone-checklist.md`, krok 11 (volitelné, mimo iPhone).

Zdroje: https://support.apple.com/guide/personal-safety/welcome/web · https://support.apple.com/105120
