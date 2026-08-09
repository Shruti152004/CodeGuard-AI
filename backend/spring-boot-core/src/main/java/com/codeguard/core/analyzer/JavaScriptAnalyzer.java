package com.codeguard.core.analyzer;

import com.codeguard.core.model.Issue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JavaScriptAnalyzer implements CodeAnalyzer {

    private static final Pattern EVAL_CALL = Pattern.compile("eval\\s*\\(");
    private static final Pattern CONSOLE_LOG = Pattern.compile("console\\.(log|warn|error)\\s*\\(");
    private static final Pattern VAR_DECLARATION = Pattern.compile("\\bvar\\s+\\w+");

    @Override
    public List<Issue> analyze(String filePath, String content) {
        List<Issue> issues = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            if (EVAL_CALL.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Eval Usage")
                        .category("SECURITY")
                        .severity("CRITICAL")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("eval evaluates JavaScript code from a string, presenting massive security vulnerability risks.")
                        .impact("RCE exploits and XSS execution possibilities.")
                        .recommendation("Refactor logic without using eval.")
                        .suggestedFix("Use JSON.parse(str) or safe parsing frameworks.")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (CONSOLE_LOG.matcher(line).find() && !filePath.contains("test")) {
                issues.add(Issue.builder()
                        .title("Console Statement")
                        .category("CODE_SMELL")
                        .severity("LOW")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("console.log leaves debug logs in production bundles, polluting client browsers.")
                        .impact("Unintentional disclosure of details in console logs.")
                        .recommendation("Remove console statements prior to build.")
                        .suggestedFix("// remove console.log(val);")
                        .source("STATIC_ANALYSIS")
                        .build());
            }

            if (VAR_DECLARATION.matcher(line).find()) {
                issues.add(Issue.builder()
                        .title("Var Declaration")
                        .category("STYLE")
                        .severity("LOW")
                        .filePath(filePath)
                        .lineNumber(lineNumber)
                        .description("var has function-scope instead of block-scope, leading to hoisting bugs.")
                        .impact("Hoisting logic defects and state leaks.")
                        .recommendation("Use let or const declarations.")
                        .suggestedFix("const value = ...;")
                        .source("STATIC_ANALYSIS")
                        .build());
            }
        }
        return issues;
    }
}
