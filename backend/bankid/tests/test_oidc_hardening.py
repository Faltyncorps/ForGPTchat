"""oidc-hardening: alg confusion, azp, iat v budoucnosti, prázdný nonce, redirect_uri jen https."""
import time

import jwt
import pytest

from backend.bankid.config import BankIdConfig
from backend.bankid.oidc import BankIdClient, OidcError
from backend.bankid.tests.test_bankid import CFG, ISS, KEY, mock_op  # noqa: F401
import httpx


def tok(**over):
    now = int(time.time())
    c = {"iss": ISS, "aud": "test-client", "sub": "u", "iat": now, "exp": now + 300, "nonce": "n"}
    c.update(over)
    return jwt.encode(c, KEY, algorithm="PS512", headers={"kid": "k1"})


@pytest.fixture
def client():
    return BankIdClient(CFG, httpx.AsyncClient())


async def test_valid_token_passes(client, mock_op):  # noqa: F811
    assert (await client.verify_id_token(tok(), "n"))["sub"] == "u"


async def test_alg_none_rejected(client, mock_op):  # noqa: F811
    now = int(time.time())
    t = jwt.encode({"iss": ISS, "aud": "test-client", "sub": "u", "iat": now, "exp": now + 60, "nonce": "n"},
                   None, algorithm="none")
    with pytest.raises(OidcError, match="algoritmus"):
        await client.verify_id_token(t, "n")


async def test_hs256_confusion_rejected(client, mock_op):  # noqa: F811
    now = int(time.time())
    t = jwt.encode({"iss": ISS, "aud": "test-client", "sub": "u", "iat": now, "exp": now + 60, "nonce": "n"},
                   "x" * 32, algorithm="HS256", headers={"kid": "k1"})
    with pytest.raises(OidcError, match="algoritmus"):
        await client.verify_id_token(t, "n")


async def test_azp_mismatch(client, mock_op):  # noqa: F811
    with pytest.raises(OidcError, match="azp"):
        await client.verify_id_token(tok(aud=["test-client", "other"], azp="other"), "n")


async def test_iat_future(client, mock_op):  # noqa: F811
    with pytest.raises(OidcError):
        await client.verify_id_token(tok(iat=int(time.time()) + 3600), "n")


async def test_empty_nonce_rejected(client, mock_op):  # noqa: F811
    with pytest.raises(OidcError, match="nonce"):
        await client.verify_id_token(tok(nonce=""), "")


def test_redirect_must_be_https(monkeypatch):
    monkeypatch.setenv("BANKID_CLIENT_ID", "x"); monkeypatch.setenv("BANKID_CLIENT_SECRET", "x")
    monkeypatch.setenv("BANKID_REDIRECT_URI", "http://evil.example/cb")
    with pytest.raises(RuntimeError, match="https"):
        BankIdConfig.from_env()
