package com.alextim.bank.common.service;

public interface JwtService {
    String generateAccessToken(String username);

    String generateRefreshToken(String username);

    String extractUsername(String token);

    long extractExpiration(String token);

    boolean isTokenValid(String token);
}
