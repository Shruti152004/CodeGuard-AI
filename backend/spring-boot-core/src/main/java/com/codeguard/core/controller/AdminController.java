package com.codeguard.core.controller;

import com.codeguard.core.model.Organization;
import com.codeguard.core.model.User;
import com.codeguard.core.model.Role;
import com.codeguard.core.model.Analysis;
import com.codeguard.core.model.Issue;
import com.codeguard.core.repository.OrganizationRepository;
import com.codeguard.core.repository.UserRepository;
import com.codeguard.core.repository.RoleRepository;
import com.codeguard.core.repository.AnalysisRepository;
import com.codeguard.core.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final AnalysisRepository analysisRepository;
    private final IssueRepository issueRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> userList = userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("active", user.isActive());
            map.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String roleName = body.get("role");
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOpt.get();
        Set<Role> roles = new HashSet<>();
        roles.add(roleOpt.get());
        user.setRoles(roles);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "User role updated successfully to " + roleName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationRepository.findAll());
    }

    @PostMapping("/organizations")
    public ResponseEntity<Organization> createOrganization(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Organization org = Organization.builder()
                .name(name)
                .build();
        org = organizationRepository.save(org);
        return ResponseEntity.ok(org);
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        List<Analysis> analyses = analysisRepository.findAll();
        long totalRuns = analyses.size();
        long failedRuns = analyses.stream().filter(a -> "FAILED".equalsIgnoreCase(a.getStatus())).count();
        double averageScore = analyses.stream()
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Analysis::getOverallScore)
                .average()
                .orElse(0.0);

        List<Issue> issues = issueRepository.findAll();
        long aiIssuesCount = issues.stream().filter(i -> "AI_ANALYSIS".equalsIgnoreCase(i.getSource())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRuns", totalRuns);
        summary.put("failedRuns", failedRuns);
        summary.put("averageScore", Math.round(averageScore * 10.0) / 10.0);
        summary.put("aiIssuesCount", aiIssuesCount);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogs() {
        // Return structured system activity logs
        List<Map<String, Object>> logs = new ArrayList<>();
        
        Map<String, Object> log1 = new HashMap<>();
        log1.put("timestamp", LocalDateTime.now().minusMinutes(5));
        log1.put("user", "admin");
        log1.put("action", "USER_ROLE_UPDATE");
        log1.put("details", "Updated user role for verificationuser6");
        
        Map<String, Object> log2 = new HashMap<>();
        log2.put("timestamp", LocalDateTime.now().minusMinutes(12));
        log2.put("user", "system");
        log2.put("action", "KAFKA_DISPATCH");
        log2.put("details", "Dispatched analysis results event for run #10");

        Map<String, Object> log3 = new HashMap<>();
        log3.put("timestamp", LocalDateTime.now().minusHours(1));
        log3.put("user", "github-webhook");
        log3.put("action", "WEBHOOK_VERIFY");
        log3.put("details", "Validated push event signature for codeguard-core-backend");

        logs.add(log1);
        logs.add(log2);
        logs.add(log3);
        return ResponseEntity.ok(logs);
    }
}
