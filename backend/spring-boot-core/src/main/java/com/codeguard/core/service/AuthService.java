package com.codeguard.core.service;

import com.codeguard.core.dto.AuthResponse;
import com.codeguard.core.dto.LoginRequest;
import com.codeguard.core.dto.RegisterRequest;
import com.codeguard.core.dto.TokenRefreshRequest;
import com.codeguard.core.model.Organization;
import com.codeguard.core.model.Role;
import com.codeguard.core.model.User;
import com.codeguard.core.repository.OrganizationRepository;
import com.codeguard.core.repository.RoleRepository;
import com.codeguard.core.repository.UserRepository;
import com.codeguard.core.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email Address already in use!");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Assign Role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("User Role not set."));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        // Handle organization if provided
        if (request.getOrganizationName() != null && !request.getOrganizationName().trim().isEmpty()) {
            String slug = request.getOrganizationName().toLowerCase().replaceAll("[^a-z0-9]", "-");
            Organization org = organizationRepository.findBySlug(slug)
                    .orElseGet(() -> organizationRepository.save(
                            Organization.builder()
                                    .name(request.getOrganizationName())
                                    .slug(slug)
                                    .build()
                    ));
            user.setOrganization(org);
        }

        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        String refresh = tokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .username(request.getUsernameOrEmail())
                .build();
    }

    public AuthResponse refreshAccessToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromJWT(refreshToken);
        String newAccessToken = tokenProvider.generateToken(username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(username)
                .build();
    }
}
