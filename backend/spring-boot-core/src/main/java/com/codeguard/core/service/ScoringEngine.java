package com.codeguard.core.service;

import com.codeguard.core.model.Issue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoringEngine {

    public static class Scores {
        public int security = 100;
        public int reliability = 100;
        public int maintainability = 100;
        public int performance = 100;
        public int codeQuality = 100;
        public int overall = 100;
    }

    public Scores calculateScores(List<Issue> issues) {
        Scores scores = new Scores();
        
        int securityDeduction = 0;
        int reliabilityDeduction = 0;
        int maintainabilityDeduction = 0;
        int performanceDeduction = 0;
        int codeQualityDeduction = 0;

        for (Issue issue : issues) {
            int penalty = getPenaltyPoints(issue.getSeverity());
            String category = issue.getCategory().toUpperCase();

            switch (category) {
                case "SECURITY":
                    securityDeduction += penalty;
                    break;
                case "BUG":
                    reliabilityDeduction += penalty;
                    break;
                case "CODE_SMELL":
                case "STYLE":
                case "MAINTAINABILITY":
                    maintainabilityDeduction += penalty;
                    break;
                case "PERFORMANCE":
                    performanceDeduction += penalty;
                    break;
                case "DUPLICATION":
                case "ARCHITECTURE":
                    codeQualityDeduction += penalty;
                    break;
                default:
                    codeQualityDeduction += penalty;
                    break;
            }
        }

        scores.security = Math.max(0, 100 - securityDeduction);
        scores.reliability = Math.max(0, 100 - reliabilityDeduction);
        scores.maintainability = Math.max(0, 100 - maintainabilityDeduction);
        scores.performance = Math.max(0, 100 - performanceDeduction);
        scores.codeQuality = Math.max(0, 100 - codeQualityDeduction);

        scores.overall = (scores.security + scores.reliability + scores.maintainability + scores.performance + scores.codeQuality) / 5;
        return scores;
    }

    private int getPenaltyPoints(String severity) {
        if (severity == null) return 1;
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return 15;
            case "HIGH":
                return 8;
            case "MEDIUM":
                return 4;
            case "LOW":
                return 1;
            default:
                return 0;
        }
    }
}
