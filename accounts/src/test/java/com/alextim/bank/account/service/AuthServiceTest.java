package com.alextim.bank.account.service;


import com.alextim.bank.account.constant.Role;
import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.exception.BadCredentialsException;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.common.exception.InvalidTokenException;
import com.alextim.bank.common.service.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {AuthServiceImpl.class})
@ActiveProfiles("test")
class AuthServiceTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private JwtServiceImpl jwtService;

    @Autowired
    private AuthServiceImpl authService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .login("ivan_ivanov")
                .password("$2a$10$hashedpassword")
                .login("ivan_ivanov")
                .firstName("ivan")
                .lastName("ivanov")
                .birthDate(LocalDate.of(1990, 8, 15))
                .roles(List.of(Role.USER))
                .build();

        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan_ivanov"))));
    }

    @Test
    void generateToken_shouldReturnTokenPair_whenCredentialsAreValidTest() {
        LoginRequest request = new LoginRequest("ivan_ivanov", "plain_password");

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("plain_password", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtService.generateAccessToken("ivan_ivanov")).thenReturn("access-dto-123");
        when(jwtService.generateRefreshToken("ivan_ivanov")).thenReturn("refresh-dto-123");

        TokenPairResponse response = authService.generateToken(request);

        assertThat(response.getAccessToken()).isEqualTo("access-dto-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-dto-123");

        verify(passwordEncoder).matches("plain_password", "$2a$10$hashedpassword");
    }

    @Test
    void generateToken_shouldThrowUsernameNotFoundExceptionTest() {
        LoginRequest request = new LoginRequest("unknown", "password");

        when(accountRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.generateToken(request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account with login unknown not found");
    }

    @Test
    void generateToken_shouldThrowBadCredentialsExceptionTest() {
        LoginRequest request = new LoginRequest("ivan_ivanov", "wrong_password");

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong_password", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.generateToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad password of account with login ivan_ivanov");
    }

    @Test
    void refreshToken_shouldReturnNewTokenPair_whenTokenIsValidTest() {
        RefreshRequest request = new RefreshRequest("valid-refresh-dto");

        when(jwtService.isTokenValid("valid-refresh-dto")).thenReturn(true);
        when(jwtService.extractUsername("valid-refresh-dto")).thenReturn("ivan_ivanov");
        when(jwtService.generateAccessToken("ivan_ivanov")).thenReturn("new-access-dto");
        when(jwtService.generateRefreshToken("ivan_ivanov")).thenReturn("new-refresh-dto");

        TokenPairResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-dto");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-dto");
        verify(jwtService).isTokenValid("valid-refresh-dto");
    }

    @Test
    void refreshToken_ShouldThrowInvalidTokenExceptionTest() {
        RefreshRequest request = new RefreshRequest("invalid-refresh-dto");

        when(jwtService.isTokenValid("invalid-refresh-dto")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshToken_ShouldThrowInvalidTokenException_WhenTokenIsNull() {
        RefreshRequest request = new RefreshRequest(null);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshToken_ShouldThrowInvalidTokenException_WhenTokenIsEmpty() {
        RefreshRequest request = new RefreshRequest("");

        when(jwtService.isTokenValid("")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class);
    }
}