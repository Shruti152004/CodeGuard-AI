package com.codeguard.core.controller;

import com.codeguard.core.dto.AuthResponse;
import com.codeguard.core.dto.LoginRequest;
import com.codeguard.core.dto.RegisterRequest;
import com.codeguard.core.dto.TokenRefreshRequest;
import com.codeguard.core.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("status", HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshAccessToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        AuthResponse response = authService.refreshAccessToken(refreshRequest);
        return ResponseEntity.ok(response);
    }
}
