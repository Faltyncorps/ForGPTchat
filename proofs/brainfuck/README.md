# Brainfuck – ELFALL umění (NE produkční kód)

Malé programy v Brainfucku „zrcadlí“ triviální invarianty ELFALL jako parodii. **Nic z toho není
bezpečnostní kontrola** – skutečné ověření PINu je v `core/src/pin.rs` (Argon2id, konstantní čas, limit pokusů).

| Soubor | Co dělá |
|--------|---------|
| `hello_flc.bf` | vypíše `FLC` |
| `flc_pin_parody.bf` | načte 1 bajt; `7` → `OK`, jinak `NO` (parodie „pin checku“; **žádný skutečný PIN**) |
| `bf.py` | minimální interpret (limit kroků) |
| `selfcheck.py` | ověří očekávané výstupy (`python3 proofs/brainfuck/selfcheck.py`) |

Sémantika: páska 30 000 buněk, 8bit wrap, `,` na EOF = 0.
