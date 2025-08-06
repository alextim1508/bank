package com.alextim.bank.common.security;


import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.property.JwtProperty;
import com.alextim.bank.common.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static com.alextim.bank.common.client.util.AuthAccountClientUtils.checkTokenStatus;
import static com.alextim.bank.common.client.util.AuthAccountClientUtils.refreshToken;


@Component
@ConditionalOnMissingBean(name = "jwtAuthenticationFilter")
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilterImpl extends OncePerRequestFilter implements JwtAuthenticationFilter {

    private final UserJwtPublicEndpointChecker publicEndpointChecker;

    private final AuthServiceClient authServiceClient;

    private final JwtService jwtService;

    private final JwtProperty jwtProperty;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("doFilterInternal");

        String requestURI = request.getRequestURI();
        log.info("JwtAuthenticationFilter for {} {}", requestURI, request.getMethod());

        if (publicEndpointChecker.isPublicRequest(requestURI)) {
            log.info("is public");
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Cookie> accessTokenCookie = extractTokenFromCookies(request, jwtProperty.getAccessTokenName());
        Optional<Cookie> refreshTokenCookie = extractTokenFromCookies(request, jwtProperty.getRefreshTokenName());

        if (accessTokenCookie.isEmpty() || refreshTokenCookie.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = accessTokenCookie.get().getValue();
        String refreshToken = refreshTokenCookie.get().getValue();

        TokenStatusResponse tokenStatusResponse = checkTokenStatus(authServiceClient, accessToken);
        log.info("tokenStatusResponse: {}", tokenStatusResponse);

        if (!tokenStatusResponse.isValid() ||
                tokenStatusResponse.isBlacklisted() ||
                tokenStatusResponse.isExpired()) {

            log.warn("Access denied: token is not valid");

            expireCookie(response, jwtProperty.getAccessTokenName());
            expireCookie(response, jwtProperty.getRefreshTokenName());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiResponse.error("Token is revoked. Please log in again", "")
                    )
            );
            return;
        }

        accessToken = refreshAccessTokenIfExpiring(response, accessToken, refreshToken);

        String extractedLogin = jwtService.extractUsername(accessToken);
        log.info("Extracted login from accessToken cookie: {}", extractedLogin);

        HttpServletRequest wrappedRequest = wrapRequestWithUserLogin(request, extractedLogin);

        filterChain.doFilter(wrappedRequest, response);
    }

    private String refreshAccessTokenIfExpiring(HttpServletResponse response,
                                                String accessToken,
                                                String refreshToken) {

        long expirationTime = jwtService.extractExpiration(accessToken);
        log.debug("Access token expiration time: {} ms", expirationTime);

        if (!isAboutToExpire(expirationTime)) {
            return accessToken;
        }

        log.info("Access token is about to expire");

        TokenPairResponse tokenPair = refreshToken(authServiceClient, new RefreshRequest(refreshToken));

        String newAccessToken = tokenPair.getAccessToken();

        ResponseCookie cookie = ResponseCookie.from(jwtProperty.getAccessTokenName(), newAccessToken)
                .path("/")
                .maxAge(Duration.ofSeconds(jwtProperty.getAccessExpiration()))
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("Access token refreshed and set in cookie");

        return newAccessToken;

    }

    private static Optional<Cookie> extractTokenFromCookies(HttpServletRequest request, String tokenName) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> tokenName.equals(cookie.getName()))
                .findFirst();
    }

    private boolean isAboutToExpire(long expirationTime) {
        return Math.abs(expirationTime - System.currentTimeMillis()) < Duration.ofSeconds(30).toMillis();
    }

    private HttpServletRequest wrapRequestWithUserLogin(HttpServletRequest request, String extractedLogin) {
        log.info("Addind  {} cookie for: {} ", jwtProperty.getLoginHeaderName(), extractedLogin);

        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (jwtProperty.getLoginHeaderName().equalsIgnoreCase(name)) {
                    return extractedLogin;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (jwtProperty.getLoginHeaderName().equalsIgnoreCase(name)) {
                    return Collections.enumeration(Arrays.asList(extractedLogin));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> headerNames = Collections.list(super.getHeaderNames());

                if (headerNames.stream().noneMatch(jwtProperty.getLoginHeaderName()::equalsIgnoreCase)) {
                    headerNames.add(jwtProperty.getLoginHeaderName());
                }

                return Collections.enumeration(headerNames);
            }
        };
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
}
