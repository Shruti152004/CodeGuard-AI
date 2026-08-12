import pytest
from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert "provider" in data

def test_analyze_empty_code():
    payload = {
        "filePath": "empty.py",
        "code": "",
        "language": "python"
    }
    response = client.post("/ai/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert len(data["issues"]) == 0
    assert data["qualityScore"] == 100

def test_analyze_mock_security_vulnerability():
    payload = {
        "filePath": "config.py",
        "code": "API_KEY = 'super_secret_secret_value'",
        "language": "python"
    }
    response = client.post("/ai/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert len(data["issues"]) >= 1
    # Check that the security vulnerability was raised
    titles = [issue["title"] for issue in data["issues"]]
    assert "Leak of Sensitive Configurations (AI Mock)" in titles
    assert data["qualityScore"] < 100

def test_analyze_python_ast_rules():
    # Test that Python AST checks (e.g. Bare except, eval/exec) run alongside AI Mock Provider
    payload = {
        "filePath": "rules.py",
        "code": "try:\n    eval('1+1')\nexcept:\n    pass",
        "language": "python"
    }
    response = client.post("/ai/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    
    titles = [issue["title"] for issue in data["issues"]]
    assert "Bare Except Clause" in titles
    assert "Dynamic Execution (eval)" in titles
    assert "Arbitrary Code Execution Hazard (AI Mock)" in titles
    assert data["qualityScore"] < 70

def test_summarize_pr():
    payload = {
        "diffs": [
            {"filename": "app.py", "patch": "@@ -1,3 +1,4 @@\n+import os"},
            {"filename": "main.py", "patch": "@@ -1,3 +1,5 @@\n+print('hello')"}
        ]
    }
    response = client.post("/ai/summarize-pr", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "summary" in data
    assert "app.py" in data["summary"]
    assert "main.py" in data["summary"]
