import hmac
import hashlib
import os
import json
from flask import Flask, jsonify, request, abort
from kafka import KafkaProducer

app = Flask(__name__)

WEBHOOK_SECRET = os.environ.get("GITHUB_WEBHOOK_SECRET", "super_secret_webhook_key_123")
KAFKA_SERVERS = os.environ.get("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

producer = None

def get_producer():
    global producer
    if producer is None:
        try:
            producer = KafkaProducer(
                bootstrap_servers=[KAFKA_SERVERS],
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                request_timeout_ms=3000,
                acks='all'
            )
            print("Successfully connected Kafka Producer to broker.")
        except Exception as e:
            print(f"Failed to connect Kafka Producer: {e}")
    return producer

def verify_signature(payload, signature_header):
    if not signature_header:
        return False
    
    sha_name, signature = signature_header.split('=')
    if sha_name != 'sha256':
        return False
    
    mac = hmac.new(WEBHOOK_SECRET.encode('utf-8'), msg=payload, digestmod=hashlib.sha256)
    return hmac.compare_digest(mac.hexdigest(), signature)

@app.route('/health', methods=['GET'])
def health():
    prod = get_producer()
    return jsonify({
        "status": "UP",
        "service": "Flask GitHub Webhooks Service",
        "kafka_connected": prod is not None
    })

@app.route('/webhook', methods=['POST'])
def handle_webhook():
    signature_header = request.headers.get('X-Hub-Signature-256')
    payload = request.data
    
    if not verify_signature(payload, signature_header):
        abort(401, description="Invalid signature")

    event_type = request.headers.get('X-GitHub-Event', 'ping')
    data = request.json or {}

    repo_name = "unknown-repo"
    branch = "main"
    
    if 'repository' in data:
        repo_name = data['repository'].get('full_name', repo_name)
    if 'ref' in data:
        ref = data['ref']
        if ref.startswith('refs/heads/'):
            branch = ref.replace('refs/heads/', '')

    event_payload = {
        "repositoryName": repo_name,
        "branch": branch,
        "gitHubToken": ""
    }

    kafka_forwarded = False
    prod = get_producer()
    if prod:
        try:
            future = prod.send("code-analysis-events", key=repo_name.encode('utf-8'), value=event_payload)
            future.get(timeout=3)
            kafka_forwarded = True
            print(f"Forwarded event '{event_type}' to Kafka topic 'code-analysis-events'")
        except Exception as e:
            print(f"Failed to forward event to Kafka: {e}")
            producer = None # Reset to reconnect next time

    return jsonify({
        "status": "accepted",
        "event": event_type,
        "kafka_forwarded": kafka_forwarded,
        "message": "Webhook processed successfully"
    }), 200

if __name__ == '__main__':
    port = int(os.environ.get("FLASK_PORT", 5000))
    app.run(host='0.0.0.0', port=port)
