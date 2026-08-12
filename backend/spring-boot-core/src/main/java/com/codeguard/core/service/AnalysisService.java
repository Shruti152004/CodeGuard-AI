package com.codeguard.core.service;

import com.codeguard.core.analyzer.AnalyzerRegistry;
import com.codeguard.core.analyzer.CodeAnalyzer;
import com.codeguard.core.model.Analysis;
import com.codeguard.core.model.AnalysisFile;
import com.codeguard.core.model.Issue;
import com.codeguard.core.model.TechnicalDebt;
import com.codeguard.core.repository.AnalysisFileRepository;
import com.codeguard.core.repository.AnalysisRepository;
import com.codeguard.core.repository.IssueRepository;
import com.codeguard.core.repository.TechnicalDebtRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AnalyzerRegistry analyzerRegistry;

    @Autowired
    private ScoringEngine scoringEngine;

    @Autowired
    private TechnicalDebtCalculator debtCalculator;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AnalysisFileRepository analysisFileRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private TechnicalDebtRepository debtRepository;

    @Autowired
    private AnalysisEventProducer eventProducer;

    @Value("${csharp.analyzer.url:http://csharp-analyzer:5001/analyze}")
    private String csharpAnalyzerUrl;

    @Value("${python.analyzer.url:http://python-ai:8000/ai/analyze}")
    private String pythonAnalyzerUrl;

    @Transactional
    public Analysis startAnalysis(String repoName, String branch, String gitHubToken) {
        Analysis analysis = Analysis.builder()
                .repositoryName(repoName)
                .branch(branch)
                .status("RUNNING")
                .build();
        analysis = analysisRepository.save(analysis);

        List<MockFile> filesToAnalyze = getFilesToAnalyze(repoName, branch, gitHubToken);
        List<CompletableFuture<List<Issue>>> futures = new ArrayList<>();

        for (MockFile file : filesToAnalyze) {
            String detectedLang = analyzerRegistry.detectLanguage(file.filePath);
            
            AnalysisFile analysisFile = AnalysisFile.builder()
                    .analysis(analysis)
                    .filePath(file.filePath)
                    .language(detectedLang)
                    .build();
            analysisFileRepository.save(analysisFile);

            // Handle analysis asynchronously in parallel
            final Analysis finalAnalysis = analysis;
            CompletableFuture<List<Issue>> future = CompletableFuture.supplyAsync(() -> {
                List<Issue> issues = new ArrayList<>();
                if (file.filePath.endsWith(".cs")) {
                    issues = analyzeCsharpExternally(file.filePath, file.content);
                } else if (file.filePath.endsWith(".py")) {
                    issues = analyzePythonExternally(file.filePath, file.content);
                } else {
                    Optional<CodeAnalyzer> localAnalyzer = analyzerRegistry.getAnalyzer(file.filePath);
                    if (localAnalyzer.isPresent()) {
                        issues = localAnalyzer.get().analyze(file.filePath, file.content);
                    }
                }
                // Map association
                for (Issue issue : issues) {
                    issue.setAnalysis(finalAnalysis);
                }
                return issues;
            });
            futures.add(future);
        }

        // Collate all issues from tasks
        List<Issue> allDetectedIssues = new ArrayList<>();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<List<Issue>> future : futures) {
                allDetectedIssues.addAll(future.get());
            }
        } catch (Exception e) {
            log.error("Error during asynchronous analysis run: {}", e.getMessage());
        }

        // Save issues to Database
        for (Issue issue : allDetectedIssues) {
            issueRepository.save(issue);
        }

        // Calculate scores and technical debt
        ScoringEngine.Scores scores = scoringEngine.calculateScores(allDetectedIssues);
        double debtHours = debtCalculator.calculateHours(allDetectedIssues);

        analysis.setSecurityScore(scores.security);
        analysis.setReliabilityScore(scores.reliability);
        analysis.setMaintainabilityScore(scores.maintainability);
        analysis.setPerformanceScore(scores.performance);
        analysis.setCodeQualityScore(scores.codeQuality);
        analysis.setOverallScore(scores.overall);
        analysis.setTechnicalDebtHours(debtHours);
        analysis.setStatus("COMPLETED");

        analysis = analysisRepository.save(analysis);

        // Update Technical Debt aggregate
        updateTechnicalDebt(repoName, debtHours);

        try {
            eventProducer.publishAnalysisResult(analysis);
        } catch (Exception ex) {
            log.error("Failed to publish analysis completed event to Kafka broker: {}", ex.getMessage());
        }

        return analysis;
    }

    private List<Issue> analyzeCsharpExternally(String filePath, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ExternalRequest req = new ExternalRequest(filePath, content, "csharp");
            HttpEntity<ExternalRequest> entity = new HttpEntity<>(req, headers);
            
            ExternalResponse response = restTemplate.postForObject(csharpAnalyzerUrl, entity, ExternalResponse.class);
            if (response != null && response.issues != null) {
                return response.issues.stream().map(this::mapDtoToIssue).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed C# Roslyn analysis call for {}: {}", filePath, e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<Issue> analyzePythonExternally(String filePath, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ExternalRequest req = new ExternalRequest(filePath, content, "python");
            HttpEntity<ExternalRequest> entity = new HttpEntity<>(req, headers);
            
            ExternalResponse response = restTemplate.postForObject(pythonAnalyzerUrl, entity, ExternalResponse.class);
            if (response != null && response.issues != null) {
                return response.issues.stream().map(this::mapDtoToIssue).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed Python AST analysis call for {}: {}", filePath, e.getMessage());
        }
        return new ArrayList<>();
    }

    private Issue mapDtoToIssue(IssueDto dto) {
        return Issue.builder()
                .title(dto.title)
                .category(dto.category)
                .severity(dto.severity)
                .filePath(dto.filePath)
                .lineNumber(dto.lineNumber)
                .description(dto.description)
                .impact(dto.impact)
                .recommendation(dto.recommendation)
                .suggestedFix(dto.suggestedFix)
                .source(dto.source != null ? dto.source : "LANGUAGE_ANALYZER")
                .build();
    }

    private void updateTechnicalDebt(String repoName, double currentRunDebt) {
        TechnicalDebt debt = debtRepository.findByRepositoryName(repoName)
                .orElse(TechnicalDebt.builder().repositoryName(repoName).totalHours(0.0).build());
        debt.setTotalHours(debt.getTotalHours() + currentRunDebt);
        debtRepository.save(debt);
    }

    private List<MockFile> getFilesToAnalyze(String repoName, String branch, String token) {
        List<MockFile> list = new ArrayList<>();
        
        list.add(new MockFile(
                "src/main/java/com/codeguard/core/SecurityFilter.java",
                "public class SecurityFilter {\n" +
                "    private String password = \"super_secret_credentials_123\";\n" +
                "    public void doFilter() {\n" +
                "        try {\n" +
                "            System.out.println(\"Filtering request\");\n" +
                "        } catch (Exception e) {} \n" +
                "    }\n" +
                "}"
        ));

        // PYTHON AST TARGET (EXTERNAL)
        list.add(new MockFile(
                "scripts/utils.py",
                "def run_script(user_input):\n" +
                "    print('Running custom script')\n" +
                "    try:\n" +
                "        eval(user_input)\n" +
                "    except:\n" +
                "        pass\n"
        ));

        list.add(new MockFile(
                "client/index.js",
                "function render() {\n" +
                "    var debug = true;\n" +
                "    console.log('Rendering content');\n" +
                "    eval('alert(1)');\n" +
                "}"
        ));

        // C# ROSLYN TARGET (EXTERNAL)
        list.add(new MockFile(
                "src/Service.cs",
                "using System;\n" +
                "public class Service {\n" +
                "    private string apiKeySecret = \"secret_value_key_123\";\n" +
                "    public void Process() {\n" +
                "        try {\n" +
                "            Console.WriteLine(\"Processing API data\");\n" +
                "        } catch (Exception ex) {}\n" +
                "    }\n" +
                "}"
        ));

        return list;
    }

    private static class MockFile {
        String filePath;
        String content;
        MockFile(String filePath, String content) {
            this.filePath = filePath;
            this.content = content;
        }
    }

    // --- JSON API STRUCTS ---

    public static class ExternalRequest {
        public String filePath;
        public String code;
        public String language;
        public ExternalRequest(String filePath, String code, String language) {
            this.filePath = filePath;
            this.code = code;
            this.language = language;
        }
    }

    public static class ExternalResponse {
        public List<IssueDto> issues;
        public int qualityScore;
        public String message;
    }

    public static class IssueDto {
        public String title;
        public String category;
        public String severity;
        public String filePath;
        public int lineNumber;
        public String description;
        public String impact;
        public String recommendation;
        public String suggestedFix;
        public String source;
    }
}
