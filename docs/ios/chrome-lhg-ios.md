# „Lhg“ (Chrome Android flags) → iPhone / iOS 27 beta pro ELFALL

Původní dokument „Lhg“ popisuje `chrome://flags` pro **Chrome na Androidu**. Na iOS **všechny prohlížeče (Safari i Chrome) používají WebKit**; `chrome://flags` na iOS ty příznaky **nemá** a nastavovat je nelze. Tento dokument říká, co z toho má na iPhonu smysl pro ELFALL (web v Safari a WKWebView v `iosApp`).

## Převodní tabulka

| Android flag (Lhg) | Na iOS | Co dělat na iPhonu / v ELFALL |
|---|---|---|
| `#enable-parallel-downloading` | **N/A** | Nic. Velké soubory stahovat přes Soubory/ShellFish. |
| `#enable-quic` | **N/A jako flag** | HTTP/3 řeší systém (URLSession/WebKit) automaticky; na serveru (Cloudflare Pages) nechat HTTP/3 zapnuté. |
| `#enable-gpu-rasterization`, `#enable-zero-copy` | **N/A** | WebKit kreslí přes Metal sám. Nativní UI ELFALL směřuje na UIKit/Metal – výkon řešit tam, ne flagem. |
| `#smooth-scrolling`, `#enable-scroll-prediction` | **N/A** | ProMotion 120 Hz řídí iOS. Ve webu se vyhnout těžkým `scroll` handlerům, používat `passive` listenery. |
| `#prerender2` | **N/A jako flag** | Ve webu lze použít `<link rel="preload">`/`preconnect`; Speculation Rules ve WebKitu ber jako nejisté – testovat, nespoléhat. |
| `#tab-strip-declutter`, `#enable-fast-unload` | **N/A** | Safari → Nastavení → Panely → automatické zavírání panelů. |
| Gemini Nano (`#optimization-guide-on-device-model`) | **N/A** | Netýká se Safari. Apple Intelligence lze vypnout v Nastavení → Apple Intelligence a Siri. |
| `#test-third-party-cookie-phaseout` | **N/A (už výchozí)** | Safari blokuje cookies třetích stran (ITP). Nastavení → Safari → *Zabránit sledování mezi weby* = zapnuto; *Pokročilá ochrana proti otiskům* = všechny prohlížení. |
| `#enable-force-dark` | **N/A** | ELFALL web má respektovat `prefers-color-scheme`; systémový tmavý režim v Nastavení → Displej. |
| PWA „Přidat na plochu“ | **Funguje** | Safari → Sdílet → *Přidat na plochu* (ELFALL web jako samostatná aplikace). |
| `chrome://gpu` diagnostika | **N/A** | Safari → Nastavení → Pokročilé → *Web Inspector* + Mac Safari, nebo Xcode u WKWebView. |

## Doporučení pro iPhone (iOS 27 beta)
1. Safari → Nastavení: *Zabránit sledování mezi weby* ✔, *Skrýt IP adresu* (před sledovači) ✔, *Upozornění na podvodné weby* ✔.
2. Safari → Nastavení → Pokročilé → *Funkce* (Feature Flags): **neměnit** pro běžné používání; jsou to experimenty WebKitu, ne ekvivalent Lhg. Případný reset: *Obnovit vše na výchozí*.
3. Pro ELFALL v WKWebView: žádné ladicí příznaky v release buildu (`isInspectable` jen v debug).
4. Režim izolace vypíná JIT – ELFALL musí zůstat použitelný i bez něj.

Pozn.: nepoužíváme žádné neověřené API „iOS 27“; vše výše funguje na aktuálních iOS a ověřte v betě.
