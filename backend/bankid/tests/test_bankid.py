"""Testy s mockovaným OIDC providerem (respx). Žádná reálná data – fiktivní osoba a testovací IBAN."""
import json
import time
from urllib.parse import parse_qs, urlparse

import httpx
import jwt
import pytest
import respx
from cryptography.hazmat.primitives.asymmetric import rsa

from backend.bankid.app import create_app
from backend.bankid.config import BankIdConfig
from backend.bankid.oidc import BankIdClient

ISS = "https://oidc.sandbox.bankid.cz/"
CFG = BankIdConfig(client_id="test-client", client_secret="test-secret", redirect_uri="https://app.test/cb")
KEY = rsa.generate_private_key(public_exponent=65537, key_size=2048)
OTHER_KEY = rsa.generate_private_key(public_exponent=65537, key_size=2048)
JWK = json.loads(jwt.algorithms.RSAAlgorithm.to_jwk(KEY.public_key()))
JWK.update(kid="k1", alg="PS512", use="sig")
TEST_IBAN = "CZ00 0000 0000 0000 0000 0000"  # fiktivní
PROFILE = {"sub": "user-1", "given_name": "Jan", "family_name": "Novák", "majority": True,
           "paymentAccounts": ["CZ0000000000000000000000"],
           "verified_claims": {"verification": {"trust_framework": "cz_aml", "verification_process": "x"}}}

DISCO = {"issuer": ISS, "authorization_endpoint": ISS + "auth", "token_endpoint": ISS + "token",
         "jwks_uri": ISS + ".well-known/jwks", "profile_endpoint": ISS + "profile"}


def id_token(nonce, *, key=KEY, aud="test-client", iss=ISS, exp_in=300, auth_time=None, acr="loa3", sub="user-1"):
    now = int(time.time())
    c = {"iss": iss, "aud": aud, "sub": sub, "iat": now, "exp": now + exp_in, "nonce": nonce, "acr": acr,
         "auth_time": auth_time if auth_time is not None else now}
    return jwt.encode(c, key, algorithm="PS512", headers={"kid": "k1"})


@pytest.fixture
def mock_op():
    with respx.mock(assert_all_called=False) as m:
        m.get(ISS + ".well-known/openid-configuration").respond(json=DISCO)
        m.get(ISS + ".well-known/jwks").respond(json={"keys": [JWK]})
        m.get(ISS + "profile").respond(json=PROFILE)
        yield m


@pytest.fixture
async def api():
    app = create_app(CFG, BankIdClient(CFG, httpx.AsyncClient()))
    async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url="http://t") as c:
        yield c


async def start(api, **body):
    r = await api.post("/auth/start", json=body)
    assert r.status_code == 200, r.text
    q = parse_qs(urlparse(r.json()["authorize_url"]).query)
    return r.json()["state"], q


def token_route(m, **kw):
    captured = {}

    def handler(req):
        captured["form"] = parse_qs(req.content.decode())
        return httpx.Response(200, json={"access_token": "at", "token_type": "Bearer",
                                         "id_token": id_token(captured["nonce"], **kw)})
    m.post(ISS + "token").mock(side_effect=handler)
    return captured


async def test_authorize_url_params(api, mock_op):
    _, q = await start(api, purpose="kyc")
    assert q["scope"] == ["openid profile.name profile.birthdate profile.verification"]
    assert q["code_challenge_method"] == ["S256"] and len(q["code_challenge"][0]) == 43
    assert q["acr_values"] == ["loa3"] and q["response_type"] == ["code"]
    assert "nonce" in q and "state" in q


async def test_kyc_flow(api, mock_op):
    state, q = await start(api, purpose="kyc")
    cap = token_route(mock_op); cap["nonce"] = q["nonce"][0]
    r = await api.post("/auth/callback", json={"code": "c", "state": state})
    assert r.status_code == 200, r.text
    assert r.json()["level"] == "bank_aml_verified"
    assert "code_verifier" in cap["form"] and cap["form"]["client_secret"] == ["test-secret"]
    assert "birthdate" not in r.json()  # minimalizace


async def test_state_single_use(api, mock_op):
    state, q = await start(api, purpose="kyc")
    cap = token_route(mock_op); cap["nonce"] = q["nonce"][0]
    assert (await api.post("/auth/callback", json={"code": "c", "state": state})).status_code == 200
    assert (await api.post("/auth/callback", json={"code": "c", "state": state})).status_code == 400


@pytest.mark.parametrize("kw,nonce_ok", [
    ({"key": OTHER_KEY}, True), ({"aud": "evil"}, True), ({"iss": "https://evil/"}, True),
    ({"exp_in": -120}, True), ({}, False)])
async def test_id_token_rejections(api, mock_op, kw, nonce_ok):
    state, q = await start(api, purpose="kyc")
    cap = token_route(mock_op, **kw); cap["nonce"] = q["nonce"][0] if nonce_ok else "wrong"
    assert (await api.post("/auth/callback", json={"code": "c", "state": state})).status_code == 401


async def test_account_ownership(api, mock_op):
    state, q = await start(api, purpose="account", payout_iban=TEST_IBAN, payout_holder="JAN NOVAK")
    assert "profile.paymentAccounts" in q["scope"][0]
    cap = token_route(mock_op); cap["nonce"] = q["nonce"][0]
    j = (await api.post("/auth/callback", json={"code": "c", "state": state})).json()
    assert j["confirmed"] and j["iban_match"] and j["name_match"]
    assert "paymentAccounts" not in j


async def test_account_mismatch(api, mock_op):
    state, q = await start(api, purpose="account", payout_iban="CZ9900000000000000000001", payout_holder="Petr X")
    cap = token_route(mock_op); cap["nonce"] = q["nonce"][0]
    j = (await api.post("/auth/callback", json={"code": "c", "state": state})).json()
    assert not j["confirmed"] and not j["name_match"]


async def test_txn_step_up(api, mock_op):
    h = "ab" * 32
    state, q = await start(api, purpose="txn", tx_hash=h)
    assert q["prompt"] == ["login"] and q["max_age"] == ["0"] and q["scope"] == ["openid"]
    cap = token_route(mock_op); cap["nonce"] = q["nonce"][0]
    j = (await api.post("/auth/callback", json={"code": "c", "state": state})).json()
    assert j["confirmed"] and j["tx_hash"] == h


async def test_txn_stale_auth_rejected(api, mock_op):
    state, q = await start(api, purpose="txn", tx_hash="cd" * 32)
    cap = token_route(mock_op, auth_time=int(time.time()) - 3600); cap["nonce"] = q["nonce"][0]
    assert (await api.post("/auth/callback", json={"code": "c", "state": state})).status_code == 401


async def test_txn_requires_hash(api, mock_op):
    assert (await api.post("/auth/start", json={"purpose": "txn", "tx_hash": "nothex"})).status_code == 400


def test_config_env(monkeypatch):
    for k in ("BANKID_CLIENT_ID", "BANKID_CLIENT_SECRET", "BANKID_REDIRECT_URI"):
        monkeypatch.setenv(k, "x")
    monkeypatch.setenv("BANKID_REDIRECT_URI", "https://app.test/cb")
    monkeypatch.setenv("BANKID_ENV", "sandbox")
    assert BankIdConfig.from_env().issuer == ISS
    monkeypatch.setenv("BANKID_ENV", "production")
    monkeypatch.delenv("BANKID_ALLOW_PRODUCTION", raising=False)
    with pytest.raises(RuntimeError):
        BankIdConfig.from_env()
