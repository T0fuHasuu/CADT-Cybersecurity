# Dockerfile — Vulnerable version
# FOR EDUCATIONAL USE ONLY
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Create logs directory
RUN mkdir -p /app/logs

ENV DB_PATH=/app/data/bank.db
ENV LOG_PATH=/app/logs/app.log
ENV PORT=5000

# Seed script runs at container startup before the app
ENTRYPOINT ["sh", "-c", "mkdir -p /app/data && python seed.py && python app.py"]
