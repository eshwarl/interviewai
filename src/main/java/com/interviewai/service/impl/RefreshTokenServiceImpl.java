package com.interviewai.service.impl;

import com.interviewai.exception.auth.InvalidCredentialsException;
import com.interviewai.model.RefreshToken;
import com.interviewai.model.User;
import com.interviewai.repository.RefreshTokenRepository;
import com.interviewai.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Refresh token valid for 7 days
    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Override
    public RefreshToken createRefreshToken(User user) {

        // Revoke existing tokens first
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Refresh token created for user: {}", user.getEmail());
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndIsRevokedFalse(token)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or revoked refresh token"));

        // Check expiry
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setIsRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired. Please login again.");
        }

        return refreshToken;
    }

    @Override
    public void revokeAllTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("All refresh tokens revoked for user: {}", user.getEmail());
    }
}