# Metal FPS – srovnání a strategie (ELFALL iOS UIKit)

> Datum: 2026-10-14 · větev `perf-ios-metal-2026-10-14` · **DRAFT technická dokumentace**

## Shrnutí

Cíl: **nejvyšší stabilní FPS** na ProMotion (až 120 Hz) při zachování zero-copy Rust→Metal
(`bytesNoCopy` + stránkové zarovnání 16 KiB) a borrow/SAFETY kontraktů v `core/src/gpu.rs`.

## „Mirror Titan SDK“ – výsledek výzkumu

**Nenalezeno.** Veřejné vyhledávání (GitHub, Apple Developer, balíčkové registry, SPM)
neobsahuje žádný open-source / komerční SDK s názvem **Mirror Titan**, **MirrorTitan**
ani **mirror-titan** určený pro iOS Metal / UIKit grafiku.

Související, ale **nesouvisející** nálezy (neadoptovat):

| Název | Co to je | Proč ne |
|-------|----------|---------|
| MirrorUE | macOS zrcadlení iPhone obrazovky (CoreMediaIO + Metal) | Není iOS SDK; jiné zařízení |
| MirrorMesh | Telepresence / face reenactment (Apple Silicon) | Jiný use-case; deepfake-adjacent |
| AvatarKit (spatialwalk) | 3D avatar rendering (Metal) | Vyžaduje appID/session token; mimo scope peněženky |
| „Mirror Titan“ (Eastman / Arredokit) | Fyzická zrcadla / nábytek / barva pouzdra | Hardware, ne software |

**Závěr:** žádné SPM vendoring. Cesta = **Apple Metal** (veřejné API).

## Reálné API a srovnání (2024–2026)

| Technologie | Role | Adoptovat? |
|-------------|------|------------|
| `MTLCommandQueue` (1× sdílená) | Fronta příkazů | Ano – už `GPU.queue` |
| Triple buffering + `DispatchSemaphore(3)` | CPU/GPU overlap, bez přepsání bufferu | **Ano – nově v `MetalCanvasView`** |
| `CAMetalLayer.maximumDrawableCount = 3` | Fronta drawable | **Ano** |
| `CADisplayLink` + `preferredFrameRateRange` / `preferredFramesPerSecond` | Až 120 FPS | **Ano – preferred = min(120, screen.max)** |
| `makeBuffer(bytesNoCopy:)` + page align | Zero-copy z Rustu | Ano – `RustGpuHandoff` / `gpu.rs` |
| `setVertexBytes` (≤4 KiB) | Bez alokace MTLBuffer | Ano – graf CPU cesta |
| `MetalTripleBufferRing` | Dynamická data >4 KiB | Ano – helper připraven |
| `MTLSharedEvent` | Sync blit/compute→render | Helper `MetalSharedSync` (volitelně) |
| Metal Performance Shaders (MPS) | Blur, convolutions, image ops | Jen pokud přibude eligible op (QR upscale zatím nearest sample) |
| MetalFX (spatial/temporal upscale) | Herní upscaling | **Ne** – UI není herní renderer; závislost na MetalFX framework |
| RealityKit ECS + command objects | 3D scény | **Ne** – UIKit + CAMetalLayer stačí |
| Game Controller | Gamepad | **Ne** – peněženka, ne hra |
| simd (`float4x4`, `simd_mix`) | Matice / animace | Ano – `ChartMath` |

## Bottleneck analýza (na papíře)

1. **CPU stall na `nextDrawable`** – pokud GPU nestíhá a není semafor → frame drops.
   Mitigace: semafor `MaxFramesInFlight=3` + skip tick při `wait(timeout: .now()) != .success`.
2. **Per-frame alokace** – `MTLRenderPassDescriptor()` a `[SIMD2]` každý snímek.
   Mitigace: `reusedPass` + `stripScratch` / `lineScratch`.
3. **Kopírování Rust→GPU** – zabráněno `bytesNoCopy` + page-aligned `PageBuffer`.
4. **Fragment cost scény** – SDF karty ve fragmentu jsou levné pro ≤16 karet; při >60 FPS
   držet `animated=false` po ustálení (už: stop DisplayLink když renderer vrátí false).
5. **Thermal** – preferovat 120 jen když animace běží; klid = DisplayLink off.

## Co je v kódu (tato větev)

- `iosApp/iosApp/MetalTripleBuffer.swift` – ring, shared event helper, page align
- `iosApp/iosApp/MetalViews.swift` – triple-buffer semafor, 120 FPS range, reuse pass/scratch
- Beze změny: `core/src/gpu.rs` zeroize / SAFETY / LIVE registr

## Bezpečnost

- Žádné tajemství v shaderech ani bufferech grafu (jen normalizované float2).
- `elfall_gpu_buffer_free` + wipe před dealloc beze změny.
- Žádný proprietary blob / closed SDK.

## Odkazy (veřejné)

- [Metal Best Practices – Triple Buffering](https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/MTLBestPracticesGuide/TripleBuffering.html)
- [Synchronizing CPU and GPU work](https://developer.apple.com/documentation/metal/synchronizing-cpu-and-gpu-work)
- [Frame Rate (iOS)](https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/MTLBestPracticesGuide/FrameRate.html)
