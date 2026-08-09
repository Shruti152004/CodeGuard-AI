package com.codeguard.core.service;

import com.codeguard.core.model.Issue;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TechnicalDebtCalculator {

    public double calculateHours(List<Issue> issues) {
        double totalHours = 0.0;
        for (Issue issue : issues) {
            totalHours += getWeightInHours(issue.getSeverity());
        }
        return totalHours;
    }

    private double getWeightInHours(String severity) {
        if (severity == null) return 0.5;
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return 4.0;
            case "HIGH":
                return 2.0;
            case "MEDIUM":
                return 1.0;
            case "LOW":
                return 0.5;
            default:
                return 0.0;
        }
    }
}
