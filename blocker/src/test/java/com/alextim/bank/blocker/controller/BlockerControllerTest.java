package com.alextim.bank.blocker.controller;


import com.alextim.bank.blocker.entity.SuspiciousOperation;
import com.alextim.bank.blocker.property.BlockerProperties;
import com.alextim.bank.blocker.repository.SuspiciousOperationRepository;
import com.alextim.bank.blocker.service.BlockerService;
import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.client.OAuth2TokenClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.AccountStatusResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.alextim.bank.blocker.constant.BlockReason.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BlockerControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlockerService blockerService;

    @Autowired
    private SuspiciousOperationRepository suspiciousOperationRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BlockerProperties blockerProperties;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private AccountServiceClient accountServiceClient;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2TokenClient oauth2TokenClient;

    @BeforeEach
    public void setUp() {
        when(notificationServiceClient.sendNotification(any()))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("OK"))));

        when(authServiceClient.checkTokenStatus("valid-token"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new TokenStatusResponse(true, false, false))));

        when(accountServiceClient.getAccountStatus(anyString()))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new AccountStatusResponse("ivan_ivanov", false))));
    }

    @AfterEach
    public void tearDown() {
        suspiciousOperationRepository.deleteAll();
    }

    @Test
    void checkOperation_ShouldReturnNotApproved_WhenSuspicious() throws Exception {
        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 5000,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-08-05T12:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"))
                .andExpect(jsonPath("$.data.approved").value(true));

        List<SuspiciousOperation> savedOps = suspiciousOperationRepository.findAll();
        assertThat(savedOps).hasSize(0);
    }

    @Test
    void checkOperation_ShouldReturnApproved_WhenNightTime() throws Exception {
        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 100,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-08-05T02:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"))
                .andExpect(jsonPath("$.data.approved").value(false));

        List<SuspiciousOperation> savedOps = suspiciousOperationRepository.findAll();
        assertThat(savedOps).hasSize(1);

        SuspiciousOperation suspiciousOperation = savedOps.get(0);
        assertThat(suspiciousOperation.getLogin()).isEqualTo("ivan_ivanov");
        assertThat(suspiciousOperation.getReasons()).contains(NIGHT_TIME.name());
    }


    @Test
    void checkOperation_ShouldReturnNotApproved_WhenHighAmount() throws Exception {
        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 11000,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-04-05T12:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(false));

        List<SuspiciousOperation> savedOps = suspiciousOperationRepository.findAll();
        assertThat(savedOps).hasSize(1);

        SuspiciousOperation suspiciousOperation = savedOps.get(0);
        assertThat(suspiciousOperation.getLogin()).isEqualTo("ivan_ivanov");
        assertThat(suspiciousOperation.getReasons()).contains(HIGH_AMOUNT.name());
    }


    @Test
    void checkOperation_ShouldReturnNotApproved_WhenTooFrequent() throws Exception {
        String login = "ivan_ivanov";
        String key = "op_count:" + login + ":DEBIT";

        redisTemplate.opsForValue().set(key, "10");
        redisTemplate.expire(key, Duration.ofMinutes(1));

        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 100,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-08-05T12:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(false));

        List<SuspiciousOperation> savedOps = suspiciousOperationRepository.findAll();
        assertThat(savedOps).hasSize(1);

        SuspiciousOperation suspiciousOperation = savedOps.get(0);
        assertThat(suspiciousOperation.getLogin()).isEqualTo("ivan_ivanov");
        assertThat(suspiciousOperation.getReasons()).contains(FREQUENT_OPS.name());
    }


    @Test
    void checkOperation_ShouldReturnNotApproved_WhenAccountLocked() throws Exception {
        when(accountServiceClient.getAccountStatus(anyString()))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new AccountStatusResponse("ivan_ivanov", true))));

        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 100,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-08-05T12:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(false));

        List<SuspiciousOperation> savedOps = suspiciousOperationRepository.findAll();
        assertThat(savedOps).hasSize(1);

        SuspiciousOperation suspiciousOperation = savedOps.get(0);
        assertThat(suspiciousOperation.getLogin()).isEqualTo("ivan_ivanov");
        assertThat(suspiciousOperation.getReasons()).contains(ACCOUNT_LOCKED.name());
    }


    @Test
    void checkOperation_ShouldReturnValidationError_WhenLoginBlank() throws Exception {
        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 100,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-04-05T12:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.details").value("{login=[Login is required]}"));
    }

    @Test
    void checkOperation_ShouldReturnValidationError_WhenAmountNegative() throws Exception {
        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": -100,
                                    "operationType": "DEBIT",
                                    "timestamp": "2025-04-05T12:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.details").value("{amount=[Amount must be greater than zero]}"));
    }

    @Test
    void checkOperation_ShouldReturnValidationError_WhenTimestampInFuture() throws Exception {
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        mockMvc.perform(post("/blocker/check-operation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "login": "ivan_ivanov",
                                    "amount": 100,
                                    "operationType": "DEBIT",
                                    "timestamp": "%s"
                                }
                                """, future)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details").value("{timestamp=[Timestamp cannot be in the future]}"));
    }

}
