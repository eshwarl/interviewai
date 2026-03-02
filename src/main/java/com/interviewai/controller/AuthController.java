package com.interviewai.controller;

import com.interviewai.dto.AuthResponse;
import com.interviewai.model.RefreshToken;
import com.interviewai.model.User;
import com.interviewai.repository.UserRepository;
import com.interviewai.security.JwtUtil;
import com.interviewai.service.RefreshTokenService;
import com.interviewai.service.UserService;
import com.interviewai.exception.auth.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    // =======================================
    // 🔥 REGISTER
    // =======================================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role) {

        User user = userService.registerUser(name, email, password, role);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build());
    }

    // =======================================
    // 🔥 LOGIN
    // =======================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam String email,
            @RequestParam String password) {

        User user = userService.loginUser(email, password);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: {}", email);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build());
    }

    // =======================================
    // 🔥 REFRESH TOKEN
    // =======================================
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {

        // Validate refresh token
        RefreshToken token = refreshTokenService.validateRefreshToken(refreshToken);
        User user = token.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        log.info("Access token refreshed for: {}", user.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // same refresh token
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build());
    }

    // =======================================
    // 🔥 LOGOUT
    // =======================================
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        refreshTokenService.revokeAllTokens(user);

        log.info("User logged out: {}", email);
        return ResponseEntity.ok("Logged out successfully");
    }
}