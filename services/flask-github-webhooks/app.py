import os
from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "UP",
        "service": "Flask GitHub Webhooks Service (Placeholder)"
    })

@app.route('/webhook', methods=['POST'])
def handle_webhook():
    # Webhook signature validation placeholder
    return jsonify({"status": "accepted", "message": "Event received"}), 202

if __name__ == '__main__':
    port = int(os.environ.get("FLASK_PORT", 5000))
    app.run(host='0.0.0.0', port=port)
