import os
import ast
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional, Dict
from ai_providers import MockAIProvider, GeminiAIProvider

app = FastAPI(title="CodeGuard AI Intelligence Service & Python AST Analyzer")

# Select Provider
api_key = os.environ.get("GEMINI_API_KEY")
if api_key:
    provider = GeminiAIProvider(api_key=api_key)
    print("Initializing Python AI service with Gemini AI Provider.")
else:
    provider = MockAIProvider()
    print("Initializing Python AI service with Mock AI Provider.")

class AnalysisRequest(BaseModel):
    filePath: Optional[str] = "unnamed.py"
    code: str
    language: str

class IssueDto(BaseModel):
    title: str
    category: str
    severity: str
    filePath: str
    lineNumber: int
    description: str
    impact: str
    recommendation: str
    suggestedFix: str
    source: str

class AnalysisResponse(BaseModel):
    issues: List[IssueDto]
    qualityScore: int
    message: str
    recommendations: Optional[List[str]] = []

class DiffItem(BaseModel):
    filename: str
    patch: str

class PRSummaryRequest(BaseModel):
    diffs: List[DiffItem]

class PRSummaryResponse(BaseModel):
    summary: str

@app.get("/health")
def health():
    return {
        "status": "UP", 
        "service": "Python AI Service", 
        "provider": provider.__class__.__name__
    }

@app.post("/ai/analyze", response_model=AnalysisResponse)
def analyze(req: AnalysisRequest):
    if not req.code.strip():
        return AnalysisResponse(
            issues=[], 
            qualityScore=100, 
            message="Empty code submitted",
            recommendations=[]
        )

    # 1. Run local static AST parser logic first if python code
    ast_issues = []
    if req.language.lower() == "python":
        try:
            root = ast.parse(req.code)
            for node in ast.walk(root):
                # Bare except blocks
                if isinstance(node, ast.ExceptHandler) and node.type is None:
                    ast_issues.append(IssueDto(
                        title="Bare Except Clause",
                        category="MAINTAINABILITY",
                        severity="MEDIUM",
                        filePath=req.filePath,
                        lineNumber=node.lineno,
                        description="Bare except clause catches system exit requests and Ctrl+C, making thread/process termination difficult.",
                        impact="Hides interpreter faults and bugs.",
                        recommendation="Catch specific exception types or at least Exception.",
                        suggestedFix="except Exception as e:\n    logging.exception(e)",
                        source="STATIC_ANALYSIS"
                    ))
                # Eval / Exec calls
                elif isinstance(node, ast.Call) and isinstance(node.func, ast.Name) and node.func.id in ("eval", "exec"):
                    ast_issues.append(IssueDto(
                        title=f"Dynamic Execution ({node.func.id})",
                        category="SECURITY",
                        severity="CRITICAL",
                        filePath=req.filePath,
                        lineNumber=node.lineno,
                        description=f"Using {node.func.id} runs arbitrary strings of python code dynamically, introducing security risks.",
                        impact="Remote Code Execution (RCE) if user inputs are parameterized.",
                        recommendation="Avoid eval/exec. Use safe dictionary/JSON lookups or AST parsers.",
                        suggestedFix="# Use safer alternatives without eval",
                        source="STATIC_ANALYSIS"
                    ))
        except Exception:
            pass # Non-parsable code will be reviewed by the AI provider anyway

    # 2. Invoke AI Provider
    ai_result = provider.analyze_code(req.code, req.language, req.filePath)
    
    # Collate and validate issues
    combined_issues = []
    for issue in ast_issues:
        combined_issues.append(issue)
        
    for issue in ai_result.get("issues", []):
        try:
            combined_issues.append(IssueDto(
                title=issue.get("title", "AI Finding"),
                category=issue.get("category", "CODE_SMELL"),
                severity=issue.get("severity", "LOW"),
                filePath=issue.get("filePath", req.filePath),
                lineNumber=issue.get("lineNumber", 1),
                description=issue.get("description", "AI Review Flag"),
                impact=issue.get("impact", "Potential issue"),
                recommendation=issue.get("recommendation", "Review code structure"),
                suggestedFix=issue.get("suggestedFix", ""),
                source="AI_ANALYSIS"
            ))
        except Exception:
            pass

    # Recalculate score based on combined issues
    score = 100
    for issue in combined_issues:
        if issue.severity == "CRITICAL":
            score -= 15
        elif issue.severity == "HIGH":
            score -= 8
        elif issue.severity == "MEDIUM":
            score -= 4
        elif issue.severity == "LOW":
            score -= 1
    score = max(0, score)

    return AnalysisResponse(
        issues=combined_issues,
        qualityScore=score,
        recommendations=ai_result.get("recommendations", []),
        message=ai_result.get("message", "AI static analysis run completed.")
    )

@app.post("/ai/summarize-pr", response_model=PRSummaryResponse)
def summarize_pr(req: PRSummaryRequest):
    diffs_list = [{"filename": item.filename, "patch": item.patch} for item in req.diffs]
    summary = provider.summarize_pull_request(diffs_list)
    return PRSummaryResponse(summary=summary)
