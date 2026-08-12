import os
import json
import logging
from abc import ABC, abstractmethod
from typing import List, Dict, Any

logger = logging.getLogger("python-ai")

class AIProvider(ABC):
    @abstractmethod
    def analyze_code(self, code: str, language: str, file_path: str) -> Dict[str, Any]:
        """
        Analyze code and return a dictionary conforming to the analysis response.
        """
        pass

    @abstractmethod
    def summarize_pull_request(self, diffs: List[Dict[str, str]]) -> str:
        """
        Generate a summary of code changes in a pull request.
        """
        pass

class MockAIProvider(AIProvider):
    def analyze_code(self, code: str, language: str, file_path: str) -> Dict[str, Any]:
        # Perform mock rule-based code intelligence checks
        issues = []
        recommendations = []
        
        # 1. Hardcoded configurations / secrets
        if "secret" in code.lower() or "password" in code.lower() or "apikey" in code.lower():
            issues.append({
                "title": "Leak of Sensitive Configurations (AI Mock)",
                "category": "SECURITY",
                "severity": "CRITICAL",
                "filePath": file_path,
                "lineNumber": 1,
                "description": "Sensitive credentials or secrets were detected in the source code.",
                "impact": "Exposes credentials to repository users and unauthorized personnel.",
                "recommendation": "Inject credentials dynamically using environment variables or secret managers.",
                "suggestedFix": "# Load from env configuration\nAPI_KEY = os.environ.get('API_KEY')",
                "source": "AI_ANALYSIS"
            })
            recommendations.append("Externalize hardcoded credentials to safeguard environment access configurations.")

        # 2. Large function check
        if code.count("\n") > 150:
            issues.append({
                "title": "High Cognitive Complexity (AI Mock)",
                "category": "MAINTAINABILITY",
                "severity": "MEDIUM",
                "filePath": file_path,
                "lineNumber": 1,
                "description": "The file contains a large amount of lines, suggesting nested structures or large functions.",
                "impact": "Reduces readability and increases debugging efforts.",
                "recommendation": "Refactor complex functions into modular, single-responsibility methods.",
                "suggestedFix": "# Split the class/function into smaller helper functions",
                "source": "AI_ANALYSIS"
            })
            recommendations.append("Refactor large functions to improve long-term maintainability.")

        # 3. Dynamic evaluations
        if "eval(" in code or "exec(" in code or "system(" in code:
            issues.append({
                "title": "Arbitrary Code Execution Hazard (AI Mock)",
                "category": "SECURITY",
                "severity": "CRITICAL",
                "filePath": file_path,
                "lineNumber": 1,
                "description": "Using dynamic execution commands opens code injection vectors.",
                "impact": "Possibility of Remote Code Execution (RCE).",
                "recommendation": "Use static parsers or lookup tables to prevent arbitrary instruction executions.",
                "suggestedFix": "# Avoid using eval/exec completely",
                "source": "AI_ANALYSIS"
            })
            recommendations.append("Audit all dynamic evaluation statements for user input sanitization.")

        if not issues:
            recommendations.append("Code structure looks clean and adheres to standard guidelines.")

        # Calculate a mock score based on severity
        score = 100
        for issue in issues:
            if issue["severity"] == "CRITICAL":
                score -= 15
            elif issue["severity"] == "HIGH":
                score -= 8
            elif issue["severity"] == "MEDIUM":
                score -= 4
            elif issue["severity"] == "LOW":
                score -= 1
        score = max(0, score)

        return {
            "issues": issues,
            "qualityScore": score,
            "recommendations": recommendations,
            "message": f"Mock AI Analysis successfully analyzed {file_path}."
        }

    def summarize_pull_request(self, diffs: List[Dict[str, str]]) -> str:
        if not diffs:
            return "No pull request changes submitted to summarize."
        
        summary = "### Pull Request Summary (Mock AI Review)\n\n"
        for diff in diffs:
            filename = diff.get("filename", "unknown_file")
            summary += f"- **{filename}**: Reviewed code modifications. Heuristics show standard structural updates.\n"
        summary += "\n**Verdict**: Safe to merge. Maintain test coverage checks."
        return summary

class GeminiAIProvider(AIProvider):
    def __init__(self, api_key: str):
        self.api_key = api_key
        try:
            import google.generativeai as genai
            genai.configure(api_key=api_key)
            self.model = genai.GenerativeModel('gemini-1.5-flash')
        except ImportError:
            logger.error("google-generativeai package not installed.")
            self.model = None

    def analyze_code(self, code: str, language: str, file_path: str) -> Dict[str, Any]:
        if not self.model:
            return MockAIProvider().analyze_code(code, language, file_path)

        prompt = f"""
You are a senior code reviewer and security auditor.
Analyze the following {language} code in the file `{file_path}` and identify potential bugs, security vulnerabilities, performance issues, code smells, and maintainability concerns.

Output your response strictly as a JSON object matching this schema:
{{
  "issues": [
    {{
      "title": "Brief issue title",
      "category": "BUG" | "SECURITY" | "PERFORMANCE" | "CODE_SMELL" | "MAINTAINABILITY" | "STYLE" | "ARCHITECTURE",
      "severity": "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "INFO",
      "filePath": "{file_path}",
      "lineNumber": 1, // Line number where issue resides
      "description": "Clear explanation of the problem",
      "impact": "What happens if this is not fixed",
      "recommendation": "How to resolve this issue",
      "suggestedFix": "Code snippet or exact replacement code",
      "source": "AI_ANALYSIS"
    }}
  ],
  "qualityScore": 85, // Integer from 0 to 100 reflecting overall code quality
  "recommendations": [
    "High level design or architecture recommendations"
  ],
  "message": "A summary message of the review"
}}

Code:
```
{code}
```
"""
        try:
            response = self.model.generate_content(
                prompt,
                generation_config={"response_mime_type": "application/json"}
            )
            data = json.loads(response.text)
            # Enforce filePath and source override
            for issue in data.get("issues", []):
                issue["filePath"] = file_path
                issue["source"] = "AI_ANALYSIS"
            return data
        except Exception as e:
            logger.error(f"Gemini API execution failed, falling back to mock provider: {e}")
            return MockAIProvider().analyze_code(code, language, file_path)

    def summarize_pull_request(self, diffs: List[Dict[str, str]]) -> str:
        if not self.model:
            return MockAIProvider().summarize_pull_request(diffs)

        diffs_str = json.dumps(diffs, indent=2)
        prompt = f"""
You are an engineering lead. Generate a high-level summary of the following pull request file diff changes.
Highlight key architecture impacts, potential risks, and outline a summary of what each modified file does.

Diffs:
{diffs_str}
"""
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            logger.error(f"Gemini API PR summary failed: {e}")
            return MockAIProvider().summarize_pull_request(diffs)
