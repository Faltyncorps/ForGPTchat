#!/usr/bin/env python3
"""Minimal Brainfuck interpreter for ELFALL art programs (non-production). Usage: bf.py prog.bf [input]"""
import sys

def run(code: str, inp: bytes = b"", max_steps: int = 1_000_000) -> bytes:
    code = [c for c in code if c in "+-<>[].,"]
    jump, stack = {}, []
    for i, c in enumerate(code):
        if c == "[": stack.append(i)
        elif c == "]":
            j = stack.pop(); jump[i], jump[j] = j, i
    if stack: raise SyntaxError("unbalanced [")
    tape, p, ip, out, k, steps = [0] * 30000, 0, 0, bytearray(), 0, 0
    while ip < len(code):
        steps += 1
        if steps > max_steps: raise RuntimeError("step limit")
        c = code[ip]
        if c == "+": tape[p] = (tape[p] + 1) % 256
        elif c == "-": tape[p] = (tape[p] - 1) % 256
        elif c == ">": p += 1
        elif c == "<": p -= 1
        elif c == ".": out.append(tape[p])
        elif c == ",": tape[p] = inp[k] if k < len(inp) else 0; k += 1
        elif c == "[" and tape[p] == 0: ip = jump[ip]
        elif c == "]" and tape[p] != 0: ip = jump[ip]
        ip += 1
    return bytes(out)

if __name__ == "__main__":
    src = open(sys.argv[1]).read()
    sys.stdout.write(run(src, (sys.argv[2] if len(sys.argv) > 2 else "").encode()).decode("latin-1"))
