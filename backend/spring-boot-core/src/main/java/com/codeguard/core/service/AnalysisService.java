package com.codeguard.core.service;

import com.codeguard.core.analyzer.AnalyzerRegistry;
import com.codeguard.core.analyzer.CodeAnalyzer;
import com.codeguard.core.dto.GitHubFileDto;
import com.codeguard.core.model.Analysis;
import com.codeguard.core.model.AnalysisFile;
import com.codeguard.core.model.Issue;
import com.codeguard.core.model.TechnicalDebt;
import com.codeguard.core.repository.AnalysisFileRepository;
import com.codeguard.core.repository.AnalysisRepository;
import com.codeguard.core.repository.IssueRepository;
import com.codeguard.core.repository.TechnicalDebtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnalysisService {

    @Autowired
    private GithubClient githubClient;

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

    @Transactional
    public Analysis startAnalysis(String repoName, String branch, String gitHubToken) {
        // Create pending run
        Analysis analysis = Analysis.builder()
                .repositoryName(repoName)
                .branch(branch)
                .status("RUNNING")
                .build();
        analysis = analysisRepository.save(analysis);

        // Fetch files (simulating files retrieval)
        // If it's a real run with a token, we could query, otherwise we run analyzer on mock templates
        List<MockFile> filesToAnalyze = getFilesToAnalyze(repoName, branch, gitHubToken);
        List<Issue> detectedIssues = new ArrayList<>();

        for (MockFile file : filesToAnalyze) {
            String detectedLang = analyzerRegistry.detectLanguage(file.filePath);
            
            AnalysisFile analysisFile = AnalysisFile.builder()
                    .analysis(analysis)
                    .filePath(file.filePath)
                    .language(detectedLang)
                    .build();
            analysisFileRepository.save(analysisFile);

            Optional<CodeAnalyzer> analyzer = analyzerRegistry.getAnalyzer(file.filePath);
            if (analyzer.isPresent()) {
                List<Issue> fileIssues = analyzer.get().analyze(file.filePath, file.content);
                for (Issue issue : fileIssues) {
                    issue.setAnalysis(analysis);
                    detectedIssues.add(issueRepository.save(issue));
                }
            }
        }

        // Calculate metrics
        ScoringEngine.Scores scores = scoringEngine.calculateScores(detectedIssues);
        double debtHours = debtCalculator.calculateHours(detectedIssues);

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

        return analysis;
    }

    private void updateTechnicalDebt(String repoName, double currentRunDebt) {
        TechnicalDebt debt = debtRepository.findByRepositoryName(repoName)
                .orElse(TechnicalDebt.builder().repositoryName(repoName).totalHours(0.0).build());
        debt.setTotalHours(debt.getTotalHours() + currentRunDebt);
        debtRepository.save(debt);
    }

    private List<MockFile> getFilesToAnalyze(String repoName, String branch, String token) {
        List<MockFile> list = new ArrayList<>();
        
        // We will include templates of bad code in Java, Python, and JavaScript to verify the analyzer logic in the runs!
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
}
