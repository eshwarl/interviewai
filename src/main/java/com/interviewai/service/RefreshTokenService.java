package com.interviewai.service;

import com.interviewai.model.RefreshToken;
import com.interviewai.model.User;

public interface RefreshTokenService {

    // 🔥 Create and save refresh token for user
    RefreshToken createRefreshToken(User user);

    // 🔥 Validate refresh token — throws if expired/revoked
    RefreshToken validateRefreshToken(String token);

    // 🔥 Revoke all tokens for user (logout)
    void revokeAllTokens(User user);
}