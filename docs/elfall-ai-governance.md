# ELFALL / Eleanor – řízení AI a schvalovací hierarchie

> Stav: návrh + kódový stub (`assistant/`). Žádný reálný API klíč není v repozitáři.

## 1. Role

| Role | Kdo | Co smí | Co nesmí |
|---|---|---|---|
| **Sprinter (analytik)** | Gemini AI | Rychle analyzovat situaci, navrhnout **možnosti A–E** s důsledky, riziky a varováními; odpovídat na dotazy uživatelů, vysvětlovat pojmy, formulovat texty | Nic schvalovat ani provádět. Nevidí seed/klíče, nepodepisuje, nemá přístup k penězům ani k systémům třetích stran |
| **Schvalovací manažer** | Fallcoin (tento bot) | Vybrat/schválit možnost u **středních** případů (úroveň 1–3), být nadřízeným a poradcem Gemini, vrátit Gemini návrh k přepracování | Schvalovat úroveň 4–5, cokoli s GDPR/osobními údaji, bezpečnostním incidentem nebo právní nejistotou |
| **Watchdog (konečná instance)** | Ivo Faltyn | Konečné rozhodnutí u **kritických** a nebezpečných případů, GDPR, nelegality, bezpečnostních průniků; může přebít jakékoli rozhodnutí níže | – |

Tok: `událost → Gemini (možnosti A–E) → ApprovalGate (váha + kategorie) → Fallcoin | Ivo → provedení obranné akce → audit log`.

## 2. Úrovně závažnosti 1–5

| Úroveň | Název | Příklady | Schvaluje |
|---|---|---|---|
| 1 | Informační | dotaz na poplatek, vysvětlení pojmu | automaticky (Gemini odpoví, Fallcoin namátkově kontroluje) |
| 2 | Nízká | opakované neplatné adresy, chyba API | Fallcoin |
| 3 | Střední | podezřelé chování relace, neobvyklá částka na testnetu, chybná konfigurace | Fallcoin |
| 4 | Vysoká | pokus o převzetí účtu, únik tokenu, podezření na phishing cílený na uživatele | **Ivo** |
| 5 | Kritická | bezpečnostní průnik, únik osobních údajů (GDPR čl. 33 – hlášení ÚOOÚ do 72 h), nelegální požadavek, ohrožení prostředků | **Ivo** (okamžitá notifikace) |

**Pevná pravidla (nelze přebít konfigurací):**
- Kategorie `gdpr`, `security_breach`, `illegal`, `legal_uncertain`, `funds_at_risk` → vždy **Ivo**, bez ohledu na úroveň.
- Fallcoin **nikdy** automaticky neschvaluje úroveň ≥ 4 ani výše uvedené kategorie.
- Když si systém není jistý (chybí úroveň, Gemini nedostupné) → eskalace o úroveň výš.

## 3. Pouze obrana – právní hranice

**Povoleno (obranné akce):** detekovat, blokovat, logovat, zneplatnit relace/tokeny, zmrazit funkce aplikace,
upozornit Iva, zachovat důkazy (hashované logy, časová razítka) pro orgány činné v trestním řízení / NÚKIB / ÚOOÚ.

**ZAKÁZÁNO:** útočné protiútoky, „hacking back“, neoprávněný přístup k cizím systémům (§ 230 TZ), dekódování cizích
peněženek, malware, nelegální sledování, automatické „odvetné“ akce. Žádná z rolí – ani Ivo – nemůže tyto akce
v systému spustit; v kódu neexistují.

## 4. Volání Gemini (placeholder)

```python
from assistant import GeminiClient, ApprovalGate, Incident
client = GeminiClient(api_key=os.environ.get("GEMINI_API_KEY"))   # bez klíče = offline stub
options = client.propose_options(incident)        # 5 možností A–E s důsledky a varováními
decision = ApprovalGate().route(incident, options) # -> AUTO | FALLCOIN | IVO
```
Reálné napojení: Google Gemini API (`generativelanguage.googleapis.com`, model např. `gemini-2.x-flash`),
klíč pouze v prostředí (`GEMINI_API_KEY`), nikdy v repozitáři ani v aplikaci. Do promptu se **nikdy** neposílá seed,
klíče, plné adresy uživatelů spojené s identitou, ani jiné osobní údaje (minimalizace dle GDPR čl. 5).

## 5. Web ↔ aplikace
Gemini funguje jako asistent mezi PWA (`web/`) a mobilní aplikací: nápověda, fráze, odpovědi. Fallcoin je jeho
nadřízený a poradce – kontroluje návrhy a schvaluje střední případy. Ivo dohlíží na vše kritické.
