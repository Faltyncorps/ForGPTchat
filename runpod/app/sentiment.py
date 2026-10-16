"""Lexicon sentiment for the ELFALL CPU worker.

Same fields as a Reflex classify response. No GPU and no external key,
so the route runs inside the existing scale-to-zero proxy.
"""
from __future__ import annotations

import math
import re
import time
from typing import Any

_WORD = re.compile(r"[0-9a-záčďéěíňóřšťúůýž?]+", re.IGNORECASE)

_POS = {
    "dobře", "dobry", "dobrý", "skvělé", "skvele", "skvělý", "skvely", "super",
    "paráda", "parada", "děkuji", "dekuji", "děkuju", "dekuju", "poctivě",
    "poctive", "dřív", "driv", "hezké", "hezke", "hezký", "hezky", "spokojen",
    "spokojená", "výborné", "vyborne", "přesně", "presne", "good", "great",
    "love", "excellent", "perfect", "thanks", "beautiful", "happy", "fast",
}
_NEG = {
    "špatně", "spatne", "špatný", "spatny", "hrozné", "hrozne", "reklamace",
    "rozbité", "rozbite", "nefunguje", "hřeje", "hreje", "bojím", "bojim",
    "sotva", "zpoždění", "zpozdeni", "drahé", "drahe", "vadný", "vadny",
    "ošklivé", "osklive", "died", "broken", "hate", "terrible", "awful",
    "slow", "worst",
}
_ASK = {
    "jak", "kdy", "kde", "proč", "proc", "kolik", "jde", "lze", "můžu", "muzu",
    "může", "muze", "vrátit", "vratit", "prosím", "prosim", "?",
}
_FLIP = {"ne", "není", "neni", "not", "no", "nikdy"}

_POS_PREFIX = ("skvěl", "skvel", "hez", "poctiv", "děk", "dek", "výborn", "vyborn")
_NEG_PREFIX = ("hře", "hre", "boj", "špat", "spat", "hrozn", "rozbit", "vadn")


def _tokens(text: str) -> list[str]:
    return [m.group(0).lower() for m in _WORD.finditer(text)]


def _hits(tokens: list[str]) -> tuple[int, int, int]:
    pos = neg = ask = 0
    flip = False
    for tok in tokens:
        if tok in _FLIP:
            flip = True
            continue
        is_pos = tok in _POS or tok.startswith(_POS_PREFIX)
        is_neg = tok in _NEG or tok.startswith(_NEG_PREFIX)
        if is_pos and not is_neg:
            if flip:
                neg += 1
            else:
                pos += 1
        elif is_neg:
            if flip:
                pos += 1
            else:
                neg += 1
        if tok in _ASK:
            ask += 1
        flip = False
    return pos, neg, ask


def _family(label: str) -> str:
    token = label.strip().lower()
    if token in {"pozitivní", "pozitivni", "positive", "pochvala", "kladný", "kladny"}:
        return "pos"
    if token in {"negativní", "negativni", "negative", "stížnost", "stiznost", "reklamace"}:
        return "neg"
    if token in {"neutrální", "neutralni", "neutral", "nic"}:
        return "neu"
    if token in {"dotaz", "otázka", "otazka", "question"}:
        return "ask"
    return "other"


def _entropy(probabilities: list[float]) -> float:
    total = 0.0
    for p in probabilities:
        if p > 0:
            total -= p * math.log2(p)
    return round(total, 6)


def classify_sentiment(
    *,
    text: str | None = None,
    prompt: str | None = None,
    labels: Any = None,
) -> dict[str, Any]:
    if labels is None:
        labels = [" pozitivní", " negativní", " neutrální"]
    if not isinstance(labels, list) or not 2 <= len(labels) <= 8:
        raise ValueError("labels musí být pole o 2 až 8 štítcích")
    clean: list[str] = []
    for item in labels:
        if not isinstance(item, str) or not item.strip() or len(item) > 48:
            raise ValueError("každý štítek musí být text do 48 znaků")
        clean.append(item if item.startswith(" ") else f" {item.strip()}")

    source = text if isinstance(text, str) and text.strip() else prompt
    if not isinstance(source, str) or not source.strip():
        raise ValueError("text nebo prompt je povinný")
    if len(source) > 2000:
        raise ValueError("text je delší než 2000 znaků")

    tokens = _tokens(source)
    pos, neg, ask = _hits(tokens)
    present = set(tokens)
    weights: list[float] = []
    for label in clean:
        fam = _family(label)
        if fam == "pos":
            weight = 1 + pos * 4
        elif fam == "neg":
            weight = 1 + neg * 4
        elif fam == "ask":
            weight = 1 + ask * 4
        elif fam == "neu":
            weight = 1 + (4 if pos == 0 and neg == 0 else 0) + (2 if pos == neg and pos > 0 else 0)
        else:
            overlap = sum(1 for part in _tokens(label) if part in present)
            weight = 1 + overlap * 3
        weights.append(float(weight))

    total = sum(weights)
    probabilities = [w / total for w in weights]
    top = max(range(len(probabilities)), key=probabilities.__getitem__)
    return {
        "id": "classify-elfall",
        "object": "classification",
        "created": int(time.time()),
        "model": "elfall-lexicon",
        "label": clean[top],
        "label_index": top,
        "labels": [
            {
                "label": label,
                "probability": round(probabilities[i], 6),
                "score": round(weights[i], 2),
                "tokens": max(1, len(label.split())),
            }
            for i, label in enumerate(clean)
        ],
        "entropy": _entropy(probabilities),
    }
