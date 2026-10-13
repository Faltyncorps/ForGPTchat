"""FastAPI služba Bank iD (sandbox).

Spuštění: uvicorn backend.bankid.app:app  (env viz .env.example)
"""
from __future__ import annotations

import re
import secrets
import time

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from .config import BankIdConfig
from .logic import account_ownership, kyc_level
from .oidc import BankIdClient, OidcError, Pending, PendingStore, pkce_pair, txn_binding

TX_HASH_RE = re.compile(r"^[0-9a-fA-F]{64}$")


class StartRequest(BaseModel):
    purpose: str = Field(pattern="^(kyc|account|txn)$")
    tx_hash: str | None = None  # SHA-256 hex kanonické podoby výběru (částka|měna|IBAN|id)
    payout_iban: str | None = None
    payout_holder: str | None = None


class CallbackRequest(BaseModel):
    code: str
    state: str


def create_app(cfg: BankIdConfig | None = None, client: BankIdClient | None = None) -> FastAPI:
    cfg = cfg or BankIdConfig.from_env()
    if cfg.env != "sandbox" and client is None:
        pass  # produkce povolena jen explicitně v from_env()
    bank = client or BankIdClient(cfg)
    store = PendingStore(ttl_s=cfg.pending_ttl_s)
    app = FastAPI(title="ELFALL Bank iD")
    app.state.store = store

    @app.post("/auth/start")
    async def start(req: StartRequest):
        if req.purpose == "txn" and not (req.tx_hash and TX_HASH_RE.match(req.tx_hash)):
            raise HTTPException(400, "tx_hash musí být SHA-256 hex")
        if req.purpose == "account" and not req.payout_iban:
            raise HTTPException(400, "chybí payout_iban")
        verifier, challenge = pkce_pair()
        state = secrets.token_urlsafe(32)
        nonce = secrets.token_urlsafe(32)
        if req.purpose == "txn":
            # nonce = H(salt|tx_hash) – transakce je kryptograficky svázána s id_tokenem
            nonce = txn_binding(req.tx_hash, nonce)
        scopes = {"kyc": cfg.kyc_scopes, "account": cfg.account_scopes, "txn": cfg.txn_scopes}[req.purpose]
        store.put(state, Pending(purpose=req.purpose, verifier=verifier, nonce=nonce, created=time.time(),
                                 tx_hash=req.tx_hash, payout_iban=req.payout_iban, payout_holder=req.payout_holder))
        url = await bank.authorize_url(scopes=scopes, state=state, nonce=nonce, challenge=challenge,
                                       step_up=req.purpose == "txn")
        return {"authorize_url": url, "state": state}

    @app.post("/auth/callback")
    async def callback(req: CallbackRequest):
        p = store.pop(req.state)
        if p is None:
            raise HTTPException(400, "neznámý nebo expirovaný state")
        try:
            tokens = await bank.exchange_code(req.code, p.verifier)
            claims = await bank.verify_id_token(
                tokens["id_token"], p.nonce,
                max_auth_age_s=cfg.step_up_max_age_s if p.purpose == "txn" else None)
            if p.purpose == "txn":
                if claims.get("acr") != "loa3":
                    raise OidcError("acr není loa3")
                return {"purpose": "txn", "confirmed": True, "tx_hash": p.tx_hash, "sub": claims["sub"]}
            profile = await bank.profile(tokens["access_token"])
            if profile.get("sub") not in (None, claims["sub"]):
                raise OidcError("sub v profilu nesouhlasí s id_tokenem")
        except OidcError as e:
            raise HTTPException(401, str(e)) from e
        if p.purpose == "kyc":
            return {"purpose": "kyc", "sub": claims["sub"], **kyc_level(profile, claims)}
        return {"purpose": "account", "sub": claims["sub"],
                **account_ownership(profile, p.payout_iban, p.payout_holder)}

    @app.get("/healthz")
    async def healthz():
        return {"ok": True, "env": cfg.env}

    return app


def _lazy_app():
    try:
        return create_app()
    except RuntimeError:
        return None


app = _lazy_app()
