FROM python:3.10-slim
WORKDIR /app
COPY app.py .
RUN pip install flask
RUN mkdir -p /app/logs
EXPOSE 5000
CMD ["python","app.py"]
