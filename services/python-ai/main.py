import ast
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI(title="CodeGuard AI Intelligence Service & Python AST Analyzer")

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

@app.get("/health")
def health():
    return {"status": "UP", "service": "Python AST Analyzer"}

@app.post("/ai/analyze", response_model=AnalysisResponse)
def analyze(req: AnalysisRequest):
    issues = []
    
    if not req.code.strip():
        return AnalysisResponse(issues=[], qualityScore=100, message="Empty code submitted")

    try:
        root = ast.parse(req.code)
    except SyntaxError as e:
        raise HTTPException(status_code=400, detail=f"Python code syntax error: {str(e)}")

    for node in ast.walk(root):
        # 1. Bare except blocks
        if isinstance(node, ast.ExceptHandler):
            if node.type is None:
                issues.append(IssueDto(
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

        # 2. Eval / Exec calls
        elif isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name) and node.func.id in ("eval", "exec"):
                issues.append(IssueDto(
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

        # 3. Print statements used for logs
        elif isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name) and node.func.id == "print":
                issues.append(IssueDto(
                    title="Print Statement",
                    category="STYLE",
                    severity="INFO",
                    filePath=req.filePath,
                    lineNumber=node.lineno,
                    description="Print statement outputs directly to stdout, missing logging metadata like timestamps.",
                    impact="Polluted logs without levels.",
                    recommendation="Replace print statements with log levels using logging library.",
                    suggestedFix="import logging\nlogging.info(...)",
                    source="STATIC_ANALYSIS"
                ))

    # Calculate quality score
    score = 100
    for issue in issues:
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
        issues=issues,
        qualityScore=score,
        message=f"Python AST analysis completed with {len(issues)} issues."
    )
