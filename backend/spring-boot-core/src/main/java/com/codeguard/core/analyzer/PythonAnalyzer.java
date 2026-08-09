package com.codeguard.core.analyzer;

import com.codeguard.core.model.Issue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PythonAnalyzer implements CodeAnalyzer {

    private static final Pattern BARE_EXCEPT = Pattern.compile("except\\s*:");
    private static final Pattern EXEC_EVAL = Pattern.compile("(exec|eval)\\s*\\(");
    private static final Pattern PRINT_LOG = Pattern.compile("print\\s*\\(");

    @Override
    public List<Issue> analyze(String filePath, String content) {
        List<Issue> issues = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            if (BARE_EXCEPT.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Bare Except Clause")
                        .category("MAINTAINABILITY")
                        .severity("MEDIUM")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Using a bare except clause catches system exit requests and Ctrl+C, making termination difficult.")
                        .impact("Hiding critical Python interpreter faults and exceptions.")
                        .recommendation("Specify Exception or the specific exception type.")
                        .suggestedFix("except Exception as e:\n    logging.exception(e)")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (EXEC_EVAL.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Dynamic Execution (exec/eval)")
                        .category("SECURITY")
                        .severity("CRITICAL")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("exec/eval executes arbitrary code strings dynamically, introducing massive remote code execution risks.")
                        .impact("RCE exploits if inputs are parameterized from user resources.")
                        .recommendation("Refactor to use dictionaries or secure parsers.")
                        .suggestedFix("Safe parsing alternative without eval.")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (PRINT_LOG.matcher(line).find() && !filePath.contains("test")) {
                issues.add(Issue.builder()
                        .title("Print Statements Used")
                        .category("STYLE")
                        .severity("INFO")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("Standard print statements dump directly to stdout and lack dynamic logging levels.")
                        .impact("Pollution of stdout stream without structure.")
                        .recommendation("Use Python standard logging package.")
                        .suggestedFix("import logging\nlogging.info(...)")
                        .source("STATIC_ANALYSIS")
                        .build());
            }
        }
        return issues;
    }
}
