"""Konfigurace výhradně z proměnných prostředí. Žádná tajemství v repozitáři."""
from __future__ import annotations

import os
from dataclasses import dataclass

ISSUERS = {
    "sandbox": "https://oidc.sandbox.bankid.cz/",
    "production": "https://oidc.bankid.cz/",
}


@dataclass(frozen=True)
class BankIdConfig:
    client_id: str
    client_secret: str
    redirect_uri: str
    env: str = "sandbox"
    # Minimální sada scopes (data minimisation). profile.paymentAccounts jen pro ověření účtu.
    kyc_scopes: tuple[str, ...] = ("openid", "profile.name", "profile.birthdate", "profile.verification")
    account_scopes: tuple[str, ...] = ("openid", "profile.name", "profile.paymentAccounts")
    txn_scopes: tuple[str, ...] = ("openid",)
    pending_ttl_s: int = 600
    step_up_max_age_s: int = 120

    @property
    def issuer(self) -> str:
        return ISSUERS[self.env]

    @property
    def discovery_url(self) -> str:
        return self.issuer + ".well-known/openid-configuration"

    @staticmethod
    def from_env() -> "BankIdConfig":
        env = os.environ.get("BANKID_ENV", "sandbox").strip().lower()
        if env not in ISSUERS:
            raise RuntimeError(f"Neznámé BANKID_ENV={env!r}")
        if env == "production" and os.environ.get("BANKID_ALLOW_PRODUCTION") != "1":
            raise RuntimeError("Produkce vyžaduje smlouvu s Bankovní identitou a BANKID_ALLOW_PRODUCTION=1")
        missing = [k for k in ("BANKID_CLIENT_ID", "BANKID_CLIENT_SECRET", "BANKID_REDIRECT_URI") if not os.environ.get(k)]
        if missing:
            raise RuntimeError("Chybí proměnné prostředí: " + ", ".join(missing))
        redirect = os.environ["BANKID_REDIRECT_URI"]
        if not (redirect.startswith("https://") or redirect.startswith("http://localhost")):
            raise RuntimeError("BANKID_REDIRECT_URI musí být https:// (výjimka jen http://localhost pro vývoj)")
        return BankIdConfig(
            client_id=os.environ["BANKID_CLIENT_ID"],
            client_secret=os.environ["BANKID_CLIENT_SECRET"],
            redirect_uri=os.environ["BANKID_REDIRECT_URI"],
            env=env,
        )
