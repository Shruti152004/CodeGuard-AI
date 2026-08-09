package com.codeguard.core.analyzer;

import com.codeguard.core.model.Issue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JavaAnalyzer implements CodeAnalyzer {

    private static final Pattern EMPTY_CATCH = Pattern.compile("catch\\s*\\(\\s*\\w+\\s+\\w+\\s*\\)\\s*\\{\\s*\\}");
    private static final Pattern HARDCODED_PASS = Pattern.compile("password\\s*=\\s*\"[^\"]+\"");
    private static final Pattern SYSTEM_OUT = Pattern.compile("System\\.(out|err)\\.print");

    @Override
    public List<Issue> analyze(String filePath, String content) {
        List<Issue> issues = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            if (EMPTY_CATCH.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Empty Catch Block")
                        .category("MAINTAINABILITY")
                        .severity("HIGH")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Empty catch blocks swallow exceptions, making debugging and troubleshooting extremely difficult.")
                        .impact("Exceptions will fail silently, leading to unpredictable application states.")
                        .recommendation("Log the exception or rethrow it appropriately.")
                        .suggestedFix("catch (Exception e) {\n    log.error(\"Exception occurred: \", e);\n    throw e;\n}")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (HARDCODED_PASS.matcher(line).find() && !filePath.contains("Test")) {
                issues.add(Issue.builder()
                        .title("Hardcoded Credentials")
                        .category("SECURITY")
                        .severity("CRITICAL")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Hardcoding sensitive credentials in source code exposes them to anyone with repository access.")
                        .impact("Vulnerability of configuration keys, risking system breach.")
                        .recommendation("Extract secrets to environment variables or credentials managers.")
                        .suggestedFix("String password = System.getenv(\"DB_PASSWORD\");")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (SYSTEM_OUT.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("System.out/err Logging")
                        .category("CODE_SMELL")
                        .severity("LOW")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Using System.out/err bypasses configured logging framework contexts, missing log timestamps and metadata.")
                        .impact("Poor production log management and indexing.")
                        .recommendation("Replace with SLF4J log statement.")
                        .suggestedFix("log.info(\"...\");")
                        .source("STATIC_ANALYSIS")
                        .build());
            }
        }
        return issues;
    }
}
