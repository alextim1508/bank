package com.alextim.bank.account.controller;


import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.BadCredentialsException;
import com.alextim.bank.account.service.AuthService;
import com.alextim.bank.account.service.BlacklistService;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.property.JwtProperty;
import com.alextim.bank.common.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/account/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    private final BlacklistService blacklistService;

    private final JwtService jwtService;

    private final JwtProperty jwtProperty;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenPairResponse>> login(@RequestBody LoginRequest request) {
        log.info("Incoming request for login");

        TokenPairResponse token = authService.generateToken(request);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication = " + authentication);
        String login = authentication.getName();
        System.out.println("login = " + login);

        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refresh(@RequestBody RefreshRequest request) {
        log.info("Incoming request for refresh");

        TokenPairResponse token = authService.refreshToken(request);

        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> checkTokenStatus(@RequestParam String token) {
        boolean isBlacklisted = blacklistService.isBlacklisted(token);
        boolean isValid = jwtService.isTokenValid(token);

        TokenStatusResponse status = TokenStatusResponse.builder()
                .valid(isValid)
                .blacklisted(isBlacklisted)
                .expired(jwtService.extractExpiration(token) < System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(ApiResponse.success(status));
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            HttpServletResponse response) {

        log.info("Logout request received");

        long expiration = jwtService.extractExpiration(accessToken);
        blacklistService.addToBlacklist(accessToken, expiration - System.currentTimeMillis());

        expireCookie(response, jwtProperty.getAccessTokenName());
        expireCookie(response, jwtProperty.getRefreshTokenName());

        return ResponseEntity.ok(ApiResponse.success( "Logged out successfully"));
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("handleBadCredentialsException", ex);

        ApiResponse<?> response = ApiResponse.error("Bad credentials", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("handleAccountNotFoundException", ex);

        ApiResponse<?> response = ApiResponse.error("Account not found", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}