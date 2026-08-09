package com.codeguard.core.analyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnalyzerRegistry {

    @Autowired
    private JavaAnalyzer javaAnalyzer;

    @Autowired
    private PythonAnalyzer pythonAnalyzer;

    @Autowired
    private JavaScriptAnalyzer jsAnalyzer;

    @Autowired
    private TypeScriptAnalyzer tsAnalyzer;

    public Optional<CodeAnalyzer> getAnalyzer(String filePath) {
        String ext = getFileExtension(filePath).toLowerCase();
        switch (ext) {
            case "java":
                return Optional.of(javaAnalyzer);
            case "py":
                return Optional.of(pythonAnalyzer);
            case "js":
                return Optional.of(jsAnalyzer);
            case "ts":
            case "tsx":
                return Optional.of(tsAnalyzer);
            default:
                return Optional.empty();
        }
    }

    public String detectLanguage(String filePath) {
        String ext = getFileExtension(filePath).toLowerCase();
        switch (ext) {
            case "java":
                return "Java";
            case "py":
                return "Python";
            case "js":
                return "JavaScript";
            case "ts":
            case "tsx":
                return "TypeScript";
            case "cs":
                return "C#";
            default:
                return "Unknown";
        }
    }

    private String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filePath.length() - 1) {
            return "";
        }
        return filePath.substring(lastIndex + 1);
    }
}
