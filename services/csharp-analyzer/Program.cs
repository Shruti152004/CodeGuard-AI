var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
var app = builder.Build();

app.MapGet("/health", () => new { Status = "UP", Service = "C# Roslyn Static Analyzer (Placeholder)" });

app.MapPost("/analyze", (AnalysisRequest request) => new {
    Issues = new string[] {},
    Score = 100,
    Message = "Static analysis mock response"
});

app.Run();

public record AnalysisRequest(string Language, string Code, string Context);
