package com.codeguard.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "CodeGuard AI Core Backend");
        status.put("timestamp", System.currentTimeMillis());

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                status.put("database", "CONNECTED");
            } else {
                status.put("database", "UNHEALTHY_RESPONSE");
            }
        } catch (Exception e) {
            status.put("database", "DISCONNECTED: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/test/protected")
    public ResponseEntity<Map<String, Object>> getProtected(java.security.Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access Granted");
        response.put("user", principal != null ? principal.getName() : "anonymous");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
