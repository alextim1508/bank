package com.alextim.bank.account.service;

import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.BadCredentialsException;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.exception.InvalidTokenException;
import com.alextim.bank.common.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.alextim.bank.common.client.util.NotificationClientUtils.sendNotification;
import static com.alextim.bank.common.constant.AggregateType.ACCOUNT;
import static com.alextim.bank.common.constant.EventType.ACCOUNT_LOGIN;
import static com.alextim.bank.common.constant.EventType.ACCOUNT_LOGIN_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;

    private final NotificationServiceClient notificationServiceClient;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthMetricsService authMetricsService;

    @Override
    public TokenPairResponse generateToken(LoginRequest request) {
        log.info("Generate token for {}", request.getLogin());

        Account account = accountRepository.findByLogin(request.getLogin()).orElseThrow(() ->
                new AccountNotFoundException(request.getLogin()));

        if (passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            log.info("Correct password of account with login {}", request.getLogin());

            String accessToken = jwtService.generateAccessToken(request.getLogin());
            String refreshToken = jwtService.generateRefreshToken(request.getLogin());
            log.info("Token generated successfully");

            sendNotification(notificationServiceClient,
                    new NotificationRequest(ACCOUNT, ACCOUNT_LOGIN, request.getLogin(),
                            "successful login"));

            authMetricsService.incrementLoginSuccess(request.getLogin());

            return new TokenPairResponse(accessToken, refreshToken);
        }

        authMetricsService.incrementLoginFailure(request.getLogin());

        sendNotification(notificationServiceClient,
                new NotificationRequest(ACCOUNT, ACCOUNT_LOGIN_FAILED, request.getLogin(),
                        "failed login"));

        throw new BadCredentialsException(request.getLogin());
    }

    @Override
    public TokenPairResponse refreshToken(RefreshRequest request) {
        log.info("Refresh dto");

        if (jwtService.isTokenValid(request.getToken())) {
            String username = jwtService.extractUsername(request.getToken());
            log.info("Correct dto for account with login {}", username);

            String newAccessToken = jwtService.generateAccessToken(username);
            String newRefreshToken = jwtService.generateRefreshToken(username);

            log.info("Token refreshed successfully");

            return new TokenPairResponse(newAccessToken, newRefreshToken);
        }

        throw new InvalidTokenException();
    }
}
