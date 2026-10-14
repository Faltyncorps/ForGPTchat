#!/usr/bin/env bash
# Ověření UniFFI Swift vazeb bez macOS: Swift 6 na Linuxu + hostitelská libelfall_core.so.
set -euo pipefail
R="$(cd "$(dirname "$0")/.." && pwd)"; T=$(mktemp -d)
(cd "$R/core" && cargo build --locked --release --features ffi --lib)
mkdir -p "$T/inc" && cp "$R/core/bindings/elfall_coreFFI.h" "$T/inc/"
sed 's/use "Darwin"//' "$R/core/bindings/elfall_coreFFI.modulemap" > "$T/inc/module.modulemap"
swiftc -O -I "$T/inc" -L "$R/core/target/release" -lelfall_core "$R/core/swift-harness/main.swift" "$R/core/bindings/elfall_core.swift" -o "$T/t"
LD_LIBRARY_PATH="$R/core/target/release" "$T/t"
