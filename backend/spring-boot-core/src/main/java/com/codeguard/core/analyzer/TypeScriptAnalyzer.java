package com.codeguard.core.analyzer;

import com.codeguard.core.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class TypeScriptAnalyzer implements CodeAnalyzer {

    @Autowired
    private JavaScriptAnalyzer jsAnalyzer;

    private static final Pattern ANY_TYPE = Pattern.compile(":\\s*any\\b");

    @Override
    public List<Issue> analyze(String filePath, String content) {
        // TS shares basic rules with JS, run JS checks first
        List<Issue> issues = new ArrayList<>(jsAnalyzer.analyze(filePath, content));
        
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            if (ANY_TYPE.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Explicit 'any' Type")
                        .category("MAINTAINABILITY")
                        .severity("MEDIUM")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Using 'any' type disables TypeScript compiler checks, defeating the purpose of using TypeScript.")
                        .impact("Bypasses compiler safety checks, leading to runtime type errors.")
                        .recommendation("Specify a proper interface or union type.")
                        .suggestedFix("Use unknown, generic type, or interface model.")
                        .source("STATIC_ANALYSIS")
                        .build());
            }
        }

        return issues;
    }
}
