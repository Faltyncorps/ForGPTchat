"""Doménová logika: KYC úroveň, vlastnictví účtu. Vrací jen minimální výsledky (GDPR)."""
from __future__ import annotations

import re
import unicodedata
from typing import Any


def _norm_name(s: str) -> str:
    s = unicodedata.normalize("NFKD", s)
    s = "".join(c for c in s if not unicodedata.combining(c))
    return " ".join(s.lower().split())


def _norm_iban(s: str) -> str:
    return re.sub(r"\s+", "", s).upper()


def kyc_level(profile: dict[str, Any], id_claims: dict[str, Any]) -> dict[str, Any]:
    """Úroveň ověření. `verified_claims.verification` (scope profile.verification) nese trust_framework
    – banka ověřila klienta dle AML (identifikace osobně / ekvivalentně)."""
    ver = (profile.get("verified_claims") or {}).get("verification") or {}
    tf = ver.get("trust_framework")
    acr = id_claims.get("acr")
    if tf and acr == "loa3":
        level = "bank_aml_verified"
    elif acr == "loa3":
        level = "authenticated_loa3"
    else:
        level = "none"
    return {
        "level": level,
        "trust_framework": tf,
        "verification_process": ver.get("verification_process"),
        "acr": acr,
        "majority": profile.get("majority"),
    }


def full_name(profile: dict[str, Any]) -> str:
    parts = [profile.get("given_name"), profile.get("middle_name"), profile.get("family_name")]
    return " ".join(p for p in parts if p) or profile.get("name", "")


def account_ownership(profile: dict[str, Any], payout_iban: str | None, payout_holder: str | None) -> dict[str, Any]:
    """Primárně porovnává IBAN výplatního účtu s claimem `paymentAccounts` (scope profile.paymentAccounts),
    sekundárně jméno majitele s ověřeným jménem. Účty uživatele se nevracejí ani neukládají."""
    accounts = [_norm_iban(a) for a in (profile.get("paymentAccounts") or []) if isinstance(a, str)]
    iban_match = bool(payout_iban) and _norm_iban(payout_iban) in accounts
    name_match = bool(payout_holder) and _norm_name(payout_holder) == _norm_name(full_name(profile))
    return {
        "iban_match": iban_match,
        "name_match": name_match,
        "confirmed": iban_match,  # vlastnictví potvrzuje jen shoda IBAN; jméno je informativní
        "accounts_claim_available": bool(accounts),
    }
