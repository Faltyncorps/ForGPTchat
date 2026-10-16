import pytest

from app.handler import dispatch
from app.sentiment import classify_sentiment


def test_complaint_is_negative():
    out = classify_sentiment(
        text="Baterie vydržela sotva dva dny a nabíječka se hřeje tak, že se jí bojím dotknout.",
    )
    assert out["label"] == " negativní"
    assert abs(sum(item["probability"] for item in out["labels"]) - 1) < 1e-6


def test_praise_and_neutral_and_question():
    praise = classify_sentiment(
        text="Dorazilo o den dřív, zabalené poctivě a přesně v té barvě, co jsem chtěl.",
    )
    assert praise["label"] == " pozitivní"
    neutral = classify_sentiment(text="Balíček přišel. Funguje. Nic dalšího k tomu nemám.")
    assert neutral["label"] == " neutrální"
    question = classify_sentiment(
        text="Dobrý den, jde tenhle model vrátit do 14 dnů, když už je rozbalený?",
        labels=[" stížnost", " pochvala", " dotaz"],
    )
    assert question["label"] == " dotaz"


@pytest.mark.asyncio
async def test_handler_sentiment_envelope():
    out = await dispatch(
        {"input": {"route": "sentiment", "text": "Nabíječka se hřeje a bojím se ji zapnout."}}
    )
    assert out["ok"] is True
    assert out["network_mode"] == "testnet"
    assert out["data"]["object"] == "classification"
    assert out["data"]["label"] == " negativní"


@pytest.mark.asyncio
async def test_handler_rejects_seed_and_empty():
    seeded = await dispatch({"input": {"route": "sentiment", "text": "ok", "seed": "abandon abandon"}})
    assert seeded["ok"] is False
    empty = await dispatch({"input": {"route": "sentiment"}})
    assert empty["ok"] is False
    assert empty["code"] == 400
