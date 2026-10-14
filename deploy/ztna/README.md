# deploy/ztna – Zero Trust síťová vrstva (**NENASAZENO**, jen návrh)

| Soubor | Co | Kdy nasadit |
|---|---|---|
| `cilium-policy.yaml` | Cilium L3–L7 politika: jen ingress → bankid, jen známé cesty, egress jen Bank iD + DNS | až bude k8s s Cilium CNI |
| `xdp_drop.c` | XDP: na veřejném rozhraní propustí jen TCP 443, ICMP a odpovědi | jen při vlastním hostingu (ne za CDN/managed LB) |
| `suricata-elfall.rules` | lokální IDS pravidla (chybějící DPoP, Bearer, `alg:none`, staré TLS, brute-force `/zt/token`) | s produkčním provozem, spolu s ET Open |

Aplikační vrstva Zero Trust je **implementovaná** v backendu (`ZeroTrust.kt`): deny-by-default,
DPoP tokeny ≤ 5 min vázané na passkey + klíč zařízení, auditní log s hash řetězem.
Žádný z těchto souborů neprovádí aktivní zásahy vůči cizím sítím (POLICY.md, pravidlo 1).
