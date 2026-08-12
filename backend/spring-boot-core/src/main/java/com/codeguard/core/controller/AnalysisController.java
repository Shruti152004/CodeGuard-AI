package com.codeguard.core.controller;

import com.codeguard.core.dto.AnalysisRequest;
import com.codeguard.core.model.Analysis;
import com.codeguard.core.model.Issue;
import com.codeguard.core.model.TechnicalDebt;
import com.codeguard.core.repository.AnalysisRepository;
import com.codeguard.core.repository.IssueRepository;
import com.codeguard.core.repository.TechnicalDebtRepository;
import com.codeguard.core.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private TechnicalDebtRepository debtRepository;

    @PostMapping("/start")
    public ResponseEntity<Analysis> startAnalysis(
            @Valid @RequestBody AnalysisRequest request,
            @RequestHeader(value = "X-GitHub-Token", required = false) String gitHubToken) {
        Analysis analysis = analysisService.startAnalysis(request.getRepositoryName(), request.getBranch(), gitHubToken);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Analysis> getAnalysisDetails(@PathVariable Long id) {
        return analysisRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/issues")
    public ResponseEntity<List<Issue>> getAnalysisIssues(@PathVariable Long id) {
        List<Issue> issues = issueRepository.findByAnalysisId(id);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/technical-debt/{*repoName}")
    public ResponseEntity<TechnicalDebt> getTechnicalDebt(@PathVariable String repoName) {
        return debtRepository.findByRepositoryName(repoName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history/{*repoName}")
    public ResponseEntity<List<Analysis>> getAnalysisHistory(@PathVariable String repoName) {
        List<Analysis> history = analysisRepository.findByRepositoryNameOrderByCreatedAtDesc(repoName);
        return ResponseEntity.ok(history);
    }
}
