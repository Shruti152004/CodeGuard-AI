using Microsoft.AspNetCore.Mvc;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/health", () => new { Status = "UP", Service = "C# Roslyn Static Analyzer" });

app.MapPost("/analyze", ([FromBody] AnalysisRequest request) => {
    var issues = new List<IssueDto>();
    
    if (string.IsNullOrWhiteSpace(request.Code)) {
        return Results.Ok(new AnalysisResponse(issues, 100, "Empty code submitted"));
    }

    try {
        var tree = CSharpSyntaxTree.ParseText(request.Code);
        var root = tree.GetRoot();

        // 1. Empty catch blocks
        var catchClauses = root.DescendantNodes().OfType<CatchClauseSyntax>();
        foreach (var catchClause in catchClauses) {
            if (catchClause.Block == null || catchClause.Block.Statements.Count == 0) {
                var lineSpan = catchClause.GetLocation().GetLineSpan();
                issues.Add(new IssueDto(
                    "Empty Catch Block",
                    "MAINTAINABILITY",
                    "HIGH",
                    request.FilePath ?? "unnamed.cs",
                    lineSpan.StartLinePosition.Line + 1,
                    "Empty catch blocks swallow exceptions, making debugging and troubleshooting extremely difficult.",
                    "Exceptions fail silently.",
                    "Log or rethrow the exception.",
                    "catch (Exception ex) { _logger.LogError(ex, \"Error occurred\"); throw; }",
                    "STATIC_ANALYSIS"
                ));
            }
        }

        // 2. Hardcoded credentials
        var variables = root.DescendantNodes().OfType<VariableDeclaratorSyntax>();
        foreach (var variable in variables) {
            var name = variable.Identifier.ValueText.ToLower();
            if ((name.Contains("password") || name.Contains("secret") || name.Contains("apikey")) &&
                variable.Initializer != null &&
                variable.Initializer.Value is LiteralExpressionSyntax literal &&
                literal.IsKind(SyntaxKind.StringLiteralExpression)) {
                
                var lineSpan = variable.GetLocation().GetLineSpan();
                issues.Add(new IssueDto(
                    "Hardcoded Credentials",
                    "SECURITY",
                    "CRITICAL",
                    request.FilePath ?? "unnamed.cs",
                    lineSpan.StartLinePosition.Line + 1,
                    "Hardcoded credentials in source code exposes secrets to repository users.",
                    "Leaking sensitive system parameters.",
                    "Use environment variables or key vault configuration providers.",
                    "var apiKey = Environment.GetEnvironmentVariable(\"API_KEY\");",
                    "STATIC_ANALYSIS"
                ));
            }
        }

        // 3. System.Console calls
        var memberAccesses = root.DescendantNodes().OfType<MemberAccessExpressionSyntax>();
        foreach (var access in memberAccesses) {
            if (access.Expression is IdentifierNameSyntax id && id.Identifier.ValueText == "Console" &&
                (access.Name.Identifier.ValueText == "Write" || access.Name.Identifier.ValueText == "WriteLine")) {
                
                var lineSpan = access.GetLocation().GetLineSpan();
                issues.Add(new IssueDto(
                    "System.Console usage",
                    "CODE_SMELL",
                    "LOW",
                    request.FilePath ?? "unnamed.cs",
                    lineSpan.StartLinePosition.Line + 1,
                    "Writing to console directly bypasses logging subsystems.",
                    "Bypasses structured system logs.",
                    "Use logger interface instances.",
                    "_logger.LogInformation(\"...\");",
                    "STATIC_ANALYSIS"
                ));
            }
        }
    }
    catch (Exception ex) {
        return Results.BadRequest(new { error = "Failed to parse syntax tree: " + ex.Message });
    }

    // Compute simple quality score
    int score = 100;
    foreach (var issue in issues) {
        if (issue.Severity == "CRITICAL") score -= 15;
        else if (issue.Severity == "HIGH") score -= 8;
        else if (issue.Severity == "MEDIUM") score -= 4;
        else if (issue.Severity == "LOW") score -= 1;
    }
    score = Math.Max(0, score);

    return Results.Ok(new AnalysisResponse(issues, score, $"C# Roslyn Static Analysis completed with {issues.Count} issues."));
});

app.Run("http://0.0.0.0:5001");

public record AnalysisRequest(
    [property: JsonPropertyName("filePath")] string FilePath,
    [property: JsonPropertyName("code")] string Code,
    [property: JsonPropertyName("language")] string Language
);

public record IssueDto(
    [property: JsonPropertyName("title")] string Title,
    [property: JsonPropertyName("category")] string Category,
    [property: JsonPropertyName("severity")] string Severity,
    [property: JsonPropertyName("filePath")] string FilePath,
    [property: JsonPropertyName("lineNumber")] int LineNumber,
    [property: JsonPropertyName("description")] string Description,
    [property: JsonPropertyName("impact")] string Impact,
    [property: JsonPropertyName("recommendation")] string Recommendation,
    [property: JsonPropertyName("suggestedFix")] string SuggestedFix,
    [property: JsonPropertyName("source")] string Source
);

public record AnalysisResponse(
    [property: JsonPropertyName("issues")] List<IssueDto> Issues,
    [property: JsonPropertyName("qualityScore")] int QualityScore,
    [property: JsonPropertyName("message")] string Message
);
