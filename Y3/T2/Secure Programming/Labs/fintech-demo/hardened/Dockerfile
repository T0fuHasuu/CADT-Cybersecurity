# Dockerfile — Hardened version
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN mkdir -p /app/logs

# Create a non-root user — hardened version runs as unprivileged user
RUN useradd -m -s /bin/bash appuser && chown -R appuser:appuser /app
USER appuser

ENV DB_PATH=/app/data/bank.db
ENV LOG_PATH=/app/logs/app.log
ENV PORT=5001
# SECRET_KEY must be injected at runtime — see docker-compose.yml
# HTTPS=0 in lab (no TLS). Set to 1 behind a TLS proxy.
ENV HTTPS=0

ENTRYPOINT ["sh", "-c", "mkdir -p /app/data && python seed.py && python app.py"]
