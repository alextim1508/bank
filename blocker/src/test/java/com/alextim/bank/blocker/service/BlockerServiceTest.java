package com.alextim.bank.blocker.service;


import com.alextim.bank.blocker.property.BlockerProperties;
import com.alextim.bank.blocker.entity.SuspiciousOperation;
import com.alextim.bank.blocker.repository.SuspiciousOperationRepository;
import com.alextim.bank.blocker.constant.BlockReason;
import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.constant.OperationType;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.AccountStatusResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@EnableConfigurationProperties(BlockerProperties.class)
@SpringBootTest(classes = {BlockerServiceImpl.class})
@ActiveProfiles("test")
class BlockerServiceTest {

    @MockitoBean
    private AccountServiceClient accountServiceClient;

    @MockitoBean
    private SuspiciousOperationRepository suspiciousOperationRepository;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOps;

    @MockitoBean
    private BlockerMetricsService blockerMetricsService;

    @Autowired
    private BlockerService blockerService;


    private OperationCheckRequest request;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void isSuspicious_ShouldReturnFalse_WhenNotSuspicious() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("500"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 12, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", false))));

        when(valueOps.increment("op_count:ivan_ivanov:CREDIT")).thenReturn(1L);
        when(valueOps.get("op_count:ivan_ivanov:CREDIT")).thenReturn("1");

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(12);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);

            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isFalse();
            verify(suspiciousOperationRepository, never()).save(any(SuspiciousOperation.class));
        }

        doNothing().when(blockerMetricsService).incrementTransferBlocked(anyString(), anyString());
    }


    @Test
    void isSuspicious_ShouldReturnTrue_WhenHighAmount() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("11000"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 12, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", false))));

        when(valueOps.increment(anyString())).thenReturn(1L);

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(12);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);

            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isTrue();
            verify(suspiciousOperationRepository).save(argThat(op ->
                    op.getReasons().contains(BlockReason.HIGH_AMOUNT.name())
            ));
        }
    }

    @Test
    void isSuspicious_ShouldReturnTrue_WhenNightTime() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("1100"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 6, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", false))));

        when(valueOps.increment(anyString())).thenReturn(1L);

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(5);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);

            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isTrue();
            verify(suspiciousOperationRepository).save(argThat(op ->
                    op.getReasons().contains(BlockReason.NIGHT_TIME.name())
            ));
        }
    }
    @Test
    void isSuspicious_ShouldReturnTrue_WhenTooFrequent() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("1100"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 6, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", false))));

        when(valueOps.increment(anyString())).thenReturn(4L);

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(5);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);

            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isTrue();
            verify(suspiciousOperationRepository).save(argThat(op ->
                    op.getReasons().contains(BlockReason.FREQUENT_OPS.name())
            ));
        }
    }
    @Test
    void isSuspicious_ShouldReturnTrue_WhenAccountLocked() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("10000"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 6, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", true))));

        when(valueOps.increment(anyString())).thenReturn(1L);

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(5);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);

            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isTrue();
            verify(suspiciousOperationRepository).save(argThat(op ->
                    op.getReasons().contains(BlockReason.ACCOUNT_LOCKED.name())
            ));
        }
    }

    @Test
    void isSuspicious_ShouldReturnTrue_WithMultipleReasons() {
        request = new OperationCheckRequest(
                "ivan_ivanov",
                new BigDecimal("11000"),
                OperationType.CREDIT,
                LocalDateTime.of(20025, 7, 24, 6, 0));

        when(accountServiceClient.getAccountStatus("ivan_ivanov"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(
                        new AccountStatusResponse("ivan_ivanov", true))));

        when(valueOps.increment(anyString())).thenReturn(5L);

        LocalDateTime fixedTime = mock(LocalDateTime.class);
        when(fixedTime.getHour()).thenReturn(5);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenAnswer(invocation -> fixedTime);
            boolean result = blockerService.isSuspicious(request);

            assertThat(result).isTrue();

            ArgumentCaptor<SuspiciousOperation> captor = ArgumentCaptor.forClass(SuspiciousOperation.class);
            verify(suspiciousOperationRepository).save(captor.capture());

            SuspiciousOperation saved = captor.getValue();
            List<String> reasons = List.of(saved.getReasons().split(","));
            assertThat(reasons).containsExactlyInAnyOrder(
                    BlockReason.ACCOUNT_LOCKED.name(),
                    BlockReason.HIGH_AMOUNT.name(),
                    BlockReason.NIGHT_TIME.name(),
                    BlockReason.FREQUENT_OPS.name()
            );
        }
    }
}