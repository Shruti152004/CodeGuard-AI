package com.codeguard.core.service;

import com.codeguard.core.analyzer.JavaAnalyzer;
import com.codeguard.core.analyzer.JavaScriptAnalyzer;
import com.codeguard.core.analyzer.PythonAnalyzer;
import com.codeguard.core.model.Issue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisEngineTests {

    private final JavaAnalyzer javaAnalyzer = new JavaAnalyzer();
    private final PythonAnalyzer pythonAnalyzer = new PythonAnalyzer();
    private final JavaScriptAnalyzer jsAnalyzer = new JavaScriptAnalyzer();
    private final ScoringEngine scoringEngine = new ScoringEngine();
    private final TechnicalDebtCalculator debtCalculator = new TechnicalDebtCalculator();

    @Test
    void testJavaAnalyzer_EmptyCatchAndCredentials() {
        String content = "public class Test {\n" +
                "    String password = \"hardcoded_value_123\";\n" +
                "    public void run() {\n" +
                "        try {\n" +
                "            System.out.println(\"logging\");\n" +
                "        } catch (Exception e) {}\n" +
                "    }\n" +
                "}";

        List<Issue> issues = javaAnalyzer.analyze("Main.java", content);
        
        assertNotNull(issues);
        assertEquals(3, issues.size()); // hardcoded pass, empty catch, System.out call
        
        boolean hasCreds = issues.stream().anyMatch(i -> i.getTitle().equals("Hardcoded Credentials"));
        boolean hasCatch = issues.stream().anyMatch(i -> i.getTitle().equals("Empty Catch Block"));
        
        assertTrue(hasCreds);
        assertTrue(hasCatch);
    }

    @Test
    void testPythonAnalyzer_BareExcept() {
        String content = "try:\n" +
                "    val = 10 / 0\n" +
                "except:\n" +
                "    pass";

        List<Issue> issues = pythonAnalyzer.analyze("script.py", content);
        assertNotNull(issues);
        assertFalse(issues.isEmpty());
        assertEquals("Bare Except Clause", issues.get(0).getTitle());
    }

    @Test
    void testJavaScriptAnalyzer_Eval() {
        String content = "function run() {\n" +
                "    eval('alert(1)');\n" +
                "}";

        List<Issue> issues = jsAnalyzer.analyze("app.js", content);
        assertNotNull(issues);
        assertFalse(issues.isEmpty());
        assertEquals("Eval Usage", issues.get(0).getTitle());
    }

    @Test
    void testScoringEngine_DeterministicMath() {
        List<Issue> issues = new ArrayList<>();
        issues.add(Issue.builder().category("SECURITY").severity("CRITICAL").build()); // -15 Security
        issues.add(Issue.builder().category("BUG").severity("HIGH").build());          // -8 Reliability
        issues.add(Issue.builder().category("PERFORMANCE").severity("MEDIUM").build()); // -4 Performance

        ScoringEngine.Scores scores = scoringEngine.calculateScores(issues);

        assertEquals(85, scores.security);
        assertEquals(92, scores.reliability);
        assertEquals(96, scores.performance);
        assertEquals(100, scores.maintainability);
        assertEquals(100, scores.codeQuality);
        assertEquals(94, scores.overall); // (85+92+96+100+100)/5 = 473/5 = 94.6 -> integer division = 94
    }

    @Test
    void testTechnicalDebtCalculator() {
        List<Issue> issues = new ArrayList<>();
        issues.add(Issue.builder().severity("CRITICAL").build()); // 4h
        issues.add(Issue.builder().severity("HIGH").build());     // 2h
        issues.add(Issue.builder().severity("MEDIUM").build());   // 1h
        issues.add(Issue.builder().severity("LOW").build());      // 0.5h

        double debt = debtCalculator.calculateHours(issues);
        assertEquals(7.5, debt);
    }
}
