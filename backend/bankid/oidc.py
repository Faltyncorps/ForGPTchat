"""OIDC authorization code flow + PKCE (S256) proti Bank iD.

Dle discovery (https://oidc.sandbox.bankid.cz/.well-known/openid-configuration):
id_token podepsán PS512, acr_values_supported=[loa3], token_endpoint_auth=client_secret_post,
code_challenge_methods S256, profile endpoint /profile, userinfo /userinfo.
"""
from __future__ import annotations

import base64
import hashlib
import secrets
import time
from dataclasses import dataclass, field
from typing import Any
from urllib.parse import urlencode

import httpx
import jwt

from .config import BankIdConfig

ALLOWED_ALGS = ["PS512"]  # dle id_token_signing_alg_values_supported


def _b64url(b: bytes) -> str:
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode()


def pkce_pair() -> tuple[str, str]:
    verifier = _b64url(secrets.token_bytes(48))
    challenge = _b64url(hashlib.sha256(verifier.encode()).digest())
    return verifier, challenge


def txn_binding(tx_hash: str, salt: str) -> str:
    """Hodnota vázající transakci do nonce: SHA256(salt|tx_hash)."""
    return _b64url(hashlib.sha256(f"{salt}|{tx_hash}".encode()).digest())


@dataclass
class Pending:
    purpose: str  # kyc | account | txn
    verifier: str
    nonce: str
    created: float
    tx_hash: str | None = None
    payout_iban: str | None = None
    payout_holder: str | None = None


@dataclass
class PendingStore:
    """In-memory úložiště rozpracovaných přihlášení (state -> Pending). Pro produkci Redis s TTL."""
    ttl_s: int = 600
    _items: dict[str, Pending] = field(default_factory=dict)

    def put(self, state: str, p: Pending) -> None:
        self._gc()
        self._items[state] = p

    def pop(self, state: str) -> Pending | None:
        self._gc()
        return self._items.pop(state, None)  # jednorázové – ochrana proti replay

    def _gc(self) -> None:
        now = time.time()
        for k in [k for k, v in self._items.items() if now - v.created > self.ttl_s]:
            del self._items[k]


class OidcError(Exception):
    pass


class BankIdClient:
    def __init__(self, cfg: BankIdConfig, http: httpx.AsyncClient | None = None):
        self.cfg = cfg
        self.http = http or httpx.AsyncClient(timeout=10)
        self._disco: dict[str, Any] | None = None
        self._jwks: dict[str, Any] | None = None

    async def discovery(self) -> dict[str, Any]:
        if self._disco is None:
            r = await self.http.get(self.cfg.discovery_url)
            r.raise_for_status()
            d = r.json()
            if d.get("issuer") != self.cfg.issuer:
                raise OidcError("issuer v discovery nesouhlasí")
            self._disco = d
        return self._disco

    async def jwks(self, refresh: bool = False) -> dict[str, Any]:
        if self._jwks is None or refresh:
            r = await self.http.get((await self.discovery())["jwks_uri"])
            r.raise_for_status()
            self._jwks = r.json()
        return self._jwks

    async def authorize_url(self, *, scopes: tuple[str, ...], state: str, nonce: str, challenge: str,
                            step_up: bool = False) -> str:
        params = {
            "client_id": self.cfg.client_id,
            "redirect_uri": self.cfg.redirect_uri,
            "response_type": "code",
            "scope": " ".join(scopes),
            "state": state,
            "nonce": nonce,
            "code_challenge": challenge,
            "code_challenge_method": "S256",
            "acr_values": "loa3",
            "ui_locales": "cs",
            "display": "page",
        }
        if step_up:
            params["prompt"] = "login"
            params["max_age"] = "0"
        else:
            params["prompt"] = "consent"
        return (await self.discovery())["authorization_endpoint"] + "?" + urlencode(params)

    async def exchange_code(self, code: str, verifier: str) -> dict[str, Any]:
        r = await self.http.post((await self.discovery())["token_endpoint"], data={
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": self.cfg.redirect_uri,
            "client_id": self.cfg.client_id,
            "client_secret": self.cfg.client_secret,  # client_secret_post
            "code_verifier": verifier,
        })
        if r.status_code != 200:
            raise OidcError(f"token endpoint {r.status_code}")
        return r.json()

    async def verify_id_token(self, id_token: str, nonce: str, *, max_auth_age_s: int | None = None) -> dict[str, Any]:
        try:
            header = jwt.get_unverified_header(id_token)
        except jwt.PyJWTError as e:
            raise OidcError("neplatná hlavička id_tokenu") from e
        # Odmítnout alg=none / HS* / jiné algoritmy ještě před výběrem klíče (alg confusion).
        if header.get("alg") not in ALLOWED_ALGS:
            raise OidcError("nepovolený algoritmus id_tokenu")
        kid = header.get("kid")
        key = await self._key_for(kid)
        try:
            claims = jwt.decode(
                id_token, key=key, algorithms=ALLOWED_ALGS, audience=self.cfg.client_id,
                issuer=self.cfg.issuer, options={"require": ["exp", "iat", "iss", "aud", "sub"]}, leeway=30,
            )
        except jwt.PyJWTError as e:
            raise OidcError(f"id_token neověřen: {e}") from e
        aud = claims.get("aud")
        if isinstance(aud, list) and len(aud) > 1 and claims.get("azp") != self.cfg.client_id:
            raise OidcError("azp nesouhlasí")
        if "azp" in claims and claims["azp"] != self.cfg.client_id:
            raise OidcError("azp nesouhlasí")
        if int(claims["iat"]) > time.time() + 30:
            raise OidcError("iat je v budoucnosti")
        if not nonce or not secrets.compare_digest(str(claims.get("nonce", "")), nonce):
            raise OidcError("nonce nesouhlasí")
        if max_auth_age_s is not None:
            at = claims.get("auth_time")
            if at is None or time.time() - int(at) > max_auth_age_s:
                raise OidcError("autentizace není čerstvá (step-up)")
        return claims

    async def _key_for(self, kid: str | None):
        for refresh in (False, True):  # rotace klíčů
            keys = [k for k in (await self.jwks(refresh=refresh)).get("keys", [])
                    if k.get("use", "sig") == "sig" and k.get("kty") == "RSA"]
            if kid is None and len(keys) != 1:
                continue  # bez kid jen při jediném podpisovém klíči
            for k in keys:
                if kid is None or k.get("kid") == kid:
                    return jwt.PyJWK(k, algorithm="PS512").key
        raise OidcError("klíč z JWKS nenalezen")

    async def profile(self, access_token: str) -> dict[str, Any]:
        r = await self.http.get((await self.discovery())["profile_endpoint"],
                                headers={"Authorization": f"Bearer {access_token}"})
        if r.status_code != 200:
            raise OidcError(f"profile endpoint {r.status_code}")
        return r.json()
