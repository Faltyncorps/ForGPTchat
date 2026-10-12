FROM python:3.12-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

ENV NETWORK=testnet
ENV PYTHONUNBUFFERED=1

# RunPod serverless entry
CMD ["python", "-u", "-m", "app.handler"]
