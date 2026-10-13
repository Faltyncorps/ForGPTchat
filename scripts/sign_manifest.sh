#!/usr/bin/env bash
# Podpis manifestu kritických hodnot – spouští POUZE vlastník (Ivo Faltýn) svým klíčem, ideálně FIDO2 (sk-ssh-ed25519).
# Použití: scripts/sign_manifest.sh ~/.ssh/id_ed25519_sk
set -euo pipefail
KEY="${1:?cesta k soukromému SSH klíči (ideálně hardware sk-ssh-ed25519)}"
cd "$(dirname "$0")/.."
python3 scripts/integrity_manifest.py generate
ssh-keygen -Y sign -f "$KEY" -n elfall-integrity integrity/MANIFEST.sha256
python3 scripts/integrity_manifest.py verify
echo "Hotovo. Commitněte integrity/MANIFEST.sha256 + .sig podepsaným commitem (git commit -S)."
