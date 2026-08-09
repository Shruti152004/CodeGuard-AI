package com.codeguard.core.analyzer;

import com.codeguard.core.model.Issue;
import java.util.List;

public interface CodeAnalyzer {
    List<Issue> analyze(String filePath, String content);
}
