# ZTNA – serverová strana

- **eBPF/XDP filtrování paketů zůstává výhradně na serveru** (Cilium/eBPF CNI, případně XDP drop při vlastním
  hostingu – viz `docs/security/ZERO-TRUST-ROADMAP.md`, bod 5). Klienti (iOS/Android/web) o něm nic nevědí a nic
  nekonfigurují; v aplikacích není žádný eBPF kód.
- Klientský šifrovaný kanál `ELFALL-CH-v1` (`core/src/channel.rs`): X25519 `crypto_kx` + XChaCha20-Poly1305
  `secretstream`, připnutý statický kx klíč backendu, `binding = BLAKE2b-256("ELFALL-CH-v1\0"‖client_pk‖server_pk)`
  je AD každé zprávy a výzva k podpisu klíčem Secure Enclave. První zpráva = krátkodobý device-bound token;
  backend ověří exp, `cnf` (otisk SE klíče) a ES256 podpis `binding`. Serverová implementace v backendu zatím chybí.

## Serverové konfigurace (jen server)
- `cilium-policy.yaml` – CiliumNetworkPolicy (eBPF, L7 allowlist pro `/passkey/*` a GET), poznámka k XDP.
- `suricata-elfall.rules` – lokální pravidla Suricata IDS (rate passkey options, traversal, seed v URI, staré TLS).
- Neověřeno proti běžícímu clusteru/Suricatě (box nemá k8s ani Suricatu).
