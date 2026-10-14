# Rocq – invarianty politiky přístupu ke klíčům

`KeyPolicy.v` (Rocq 9, `rocq compile KeyPolicy.v`, CI job `rocq`) je **model** pravidla,
které vynucuje backend (`ZeroTrust.kt`) a mobilní aplikace.

## Co je dokázáno
1. `auth_sound` – podpis/operaci autorizuje **jen** dvojice *passkey + klíč zařízení*, oba vázané
   na vlastníka, oba registrované a oba neodvolané.
2. `no_device_only`, `foreign_key_rejected` – samotný klíč zařízení ani cizí klíč nestačí.
3. `revoked_never` – odvolaný klíč (passkey nebo zařízení) neautorizuje.
4. `revoked_forever` – po odvolání klíč neautorizuje v **žádném** budoucím stavu, ať následuje
   jakákoli posloupnost registrací a odvolání (odvolání nelze vrátit, registrace odvolaný klíč neobnoví);
   vlastník se operacemi nemění (`run_keeps_owner`).

## Co dokázáno NENÍ
- Že Kotlin backend model přesně implementuje (kryje to jen testy `ZeroTrustTest`).
- Kryptografie (podpisy WebAuthn, DPoP JWS) – předpokládá se, že ověření podpisu je správné.
- Perzistence/synchronizace seznamu odvolaných klíčů mezi instancemi, časové útoky, ztráta zařízení.
