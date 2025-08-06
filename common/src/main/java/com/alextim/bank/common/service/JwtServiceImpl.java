package com.alextim.bank.common.service;


import com.alextim.bank.common.exception.InvalidTokenException;
import com.alextim.bank.common.property.JwtProperty;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtProperty jwtProperty;

    @Override
    public String generateAccessToken(String username) {
        log.info("Generate token for {}", username);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperty.getAccessExpiration() * 1000L))
                    .signWith(SignatureAlgorithm.HS512, jwtProperty.getSecret())
                    .compact();
            log.info("Token generated successfully for {}", username);
            return token;
        } catch (JwtException ex) {
            log.error("Generate token error ", ex);
            throw new InvalidTokenException();
        }
    }

    @Override
    public String generateRefreshToken(String username) {
        log.info("generate refresh token for {}", username);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperty.getRefreshExpiration() * 1000L))
                    .signWith(SignatureAlgorithm.HS512, jwtProperty.getSecret())
                    .compact();
            log.info("Refresh token generated successfully for {}", username);
            return token;
        } catch (JwtException ex) {
            log.error("Generate refresh token error ", ex);
            throw new InvalidTokenException();
        }
    }

    @Override
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperty.getSecret())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException ex) {
            log.error("extract username error", ex);
            throw new InvalidTokenException();
        }
    }

    @Override
    public long extractExpiration(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperty.getSecret())
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .getTime();
        } catch (JwtException ex) {
            log.error("extract expiration error", ex);
            throw new InvalidTokenException();
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperty.getSecret())
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            log.error("is token valid", ex);
            return false;
        }
    }

    public static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return token;
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}