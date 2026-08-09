import pytest
import hmac
import hashlib
import json
from app import app, WEBHOOK_SECRET

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_health(client):
    rv = client.get('/health')
    assert rv.status_code == 200
    json_data = rv.get_json()
    assert json_data['status'] == 'UP'

def test_webhook_valid_signature(client):
    payload = json.dumps({"zen": "Keep it simple, stupid."})
    mac = hmac.new(WEBHOOK_SECRET.encode('utf-8'), msg=payload.encode('utf-8'), digestmod=hashlib.sha256)
    signature = f"sha256={mac.hexdigest()}"
    
    headers = {
        'X-Hub-Signature-256': signature,
        'X-GitHub-Event': 'ping',
        'Content-Type': 'application/json'
    }
    
    rv = client.post('/webhook', data=payload, headers=headers)
    assert rv.status_code == 200
    assert rv.get_json()['status'] == 'accepted'

def test_webhook_invalid_signature(client):
    payload = json.dumps({"zen": "Keep it simple, stupid."})
    headers = {
        'X-Hub-Signature-256': 'sha256=invalid_hash_value',
        'X-GitHub-Event': 'ping',
        'Content-Type': 'application/json'
    }
    
    rv = client.post('/webhook', data=payload, headers=headers)
    assert rv.status_code == 401

def test_webhook_missing_signature(client):
    payload = json.dumps({"zen": "Keep it simple, stupid."})
    headers = {
        'X-GitHub-Event': 'ping',
        'Content-Type': 'application/json'
    }
    
    rv = client.post('/webhook', data=payload, headers=headers)
    assert rv.status_code == 401
