# Rocq (Coq 8.20) – rámování podpisu a registr GPU bufferů

`coqc Framing.v` (ověřeno lokálně, Coq 8.20.1). Bez `Admitted`.
- `unframe_frame`, `unframe_sound`, `frame_injective`, `frame_len` – model `core/src/framing.rs`
  (`ELFALL-SIG-v1\0` ‖ BE32 délka ‖ zpráva). Kódování délky je parametr se dvěma předpoklady
  (délka 4 B, dekódování ∘ kódování = id do MAX) – pro BE u32 platí, v Rustu kryto testy.
- `no_double_free`, `foreign_pointer_rejected` – model registru `LIVE` v `core/src/gpu.rs`.
Model není extrahovaný kód: shodu s Rustem drží testy (`core/tests/`), ne důkaz.
