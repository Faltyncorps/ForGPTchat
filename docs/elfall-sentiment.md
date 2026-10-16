# ELFALL sentiment

Route na stávajícím RunPod workeru (`runpod/app/handler.py`). Žádný nový endpoint, žádný GPU worker, žádný klíč.

```json
{
  "input": {
    "route": "sentiment",
    "text": "Baterie vydržela sotva dva dny a nabíječka se hřeje.",
    "labels": [" pozitivní", " negativní", " neutrální"]
  }
}
```

Odpověď je obálka workeru: `ok`, `network_mode: testnet`, `data` ve tvaru klasifikace (`label`, `labels[].probability`, `score`, `entropy`).

Skóre počítá lokální lexikon (`runpod/app/sentiment.py`), ať route běží na CPU proxy při scale-to-zero. Semena a privátní klíče worker odmítá.

Lokálně, bez deploye:

```bash
python local_runner.py sentiment "Nabíječka se hřeje a bojím se ji zapnout."
```

Serverless deploy na RunPod stále nepouštět, dokud na účtu není kredit (`docs/elfall-network.md`).
