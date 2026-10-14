#!/usr/bin/env python3
"""Self-check BF art programs (expected outputs). Exit 1 on mismatch."""
import pathlib, sys
sys.path.insert(0, str(pathlib.Path(__file__).parent))
from bf import run
D = pathlib.Path(__file__).parent
CASES = [("hello_flc.bf", b"", b"FLC\n"), ("flc_pin_parody.bf", b"7", b"OK\n"), ("flc_pin_parody.bf", b"3", b"NO\n")]
bad = 0
for f, i, want in CASES:
    got = run((D / f).read_text(), i)
    print(("ok  " if got == want else "FAIL"), f, i, got)
    bad += got != want
sys.exit(1 if bad else 0)
