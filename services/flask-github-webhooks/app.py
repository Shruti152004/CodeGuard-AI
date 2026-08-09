import hmac
import hashlib
import os
from flask import Flask, jsonify, request, abort

app = Flask(__name__)

# Fetch secret key from environment
WEBHOOK_SECRET = os.environ.get("GITHUB_WEBHOOK_SECRET", "super_secret_webhook_key_123")

def verify_signature(payload, signature_header):
    if not signature_header:
        return False
    
    # Header format is sha256=<signature>
    sha_name, signature = signature_header.split('=')
    if sha_name != 'sha256':
        return False
    
    mac = hmac.new(WEBHOOK_SECRET.encode('utf-8'), msg=payload, digestmod=hashlib.sha256)
    return hmac.compare_digest(mac.hexdigest(), signature)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "UP",
        "service": "Flask GitHub Webhooks Service"
    })

@app.route('/webhook', methods=['POST'])
def handle_webhook():
    signature_header = request.headers.get('X-Hub-Signature-256')
    payload = request.data
    
    if not verify_signature(payload, signature_header):
        abort(401, description="Invalid signature")

    event_type = request.headers.get('X-GitHub-Event', 'ping')
    data = request.json or {}

    # We will process the webhook events here
    # For Phase 3, we log the event type and return success.
    # In Phase 6, these will be forwarded to Kafka.
    print(f"Received GitHub webhook event: {event_type}")

    return jsonify({
        "status": "accepted",
        "event": event_type,
        "message": "Webhook processed successfully"
    }), 200

if __name__ == '__main__':
    port = int(os.environ.get("FLASK_PORT", 5000))
    app.run(host='0.0.0.0', port=port)
