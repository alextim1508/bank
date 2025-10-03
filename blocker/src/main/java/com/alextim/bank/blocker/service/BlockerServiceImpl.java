package com.alextim.bank.blocker.service;

import com.alextim.bank.blocker.property.BlockerProperties;
import com.alextim.bank.blocker.property.BlockerProperties.Night;
import com.alextim.bank.blocker.constant.BlockReason;
import com.alextim.bank.blocker.entity.SuspiciousOperation;
import com.alextim.bank.blocker.repository.SuspiciousOperationRepository;
import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.constant.OperationType;
import com.alextim.bank.common.dto.account.AccountStatusResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.alextim.bank.common.client.util.AccountClientUtils.getAccountStatus;


@Service
@RequiredArgsConstructor
@Slf4j
public class BlockerServiceImpl implements BlockerService{

    private final AccountServiceClient accountServiceClient;

    private final SuspiciousOperationRepository suspiciousOperationRepository;

    private final StringRedisTemplate opCountTemplate;

    private final BlockerProperties blockerProperties;

    private final BlockerMetricsService blockerMetricsService;

    @Override
    public boolean isSuspicious(OperationCheckRequest request) {
        log.info("Checking if operation is suspicious: {}", request);

        List<BlockReason> reasons = new ArrayList<>();

        AccountStatusResponse accountLocked = getAccountStatus(accountServiceClient, request.getLogin());
        log.info("Account locked: {}", accountLocked);

        if(accountLocked.getIsBlocked()) {
            reasons.add(BlockReason.ACCOUNT_LOCKED);
        }

        boolean isHighAmount = isHighAmount(request.getAmount());
        log.info("Is high amount: {}", isHighAmount);
        if (isHighAmount) {
            reasons.add(BlockReason.HIGH_AMOUNT);
        }

        boolean isNightOperation = isNightOperation(request.getTimestamp());
        log.info("Is night operation: {}", isNightOperation);
        if (isNightOperation) {
            reasons.add(BlockReason.NIGHT_TIME);
        }

        boolean isTooFrequent = isTooFrequent(request.getLogin(), request.getOperationType());
        log.info("Is too frequent: {}", isTooFrequent);
        if (isTooFrequent) {
            reasons.add(BlockReason.FREQUENT_OPS);
        }

        boolean isSuspicious = !reasons.isEmpty();

        if (isSuspicious) {
            saveSuspiciousOperation(request, reasons);
            blockerMetricsService.incrementTransferBlocked(request.getLogin(), reasons.get(0).name());
        }

        log.info("Suspicious operation check result: login={}, isSuspicious={}, reasons={}",
                request.getLogin(), isSuspicious, reasons);

        return isSuspicious;
    }

    private boolean isHighAmount(BigDecimal amount) {
        BigDecimal maxAmount = blockerProperties.getMaxAmount();
        boolean isHigh = amount != null && amount.compareTo(maxAmount) > 0;
        log.info("High amount check: amount={}, maxAmount={}, result={}", amount, maxAmount, isHigh);
        return isHigh;
    }

    private boolean isNightOperation(LocalDateTime localDateTime) {
        int currentHour = localDateTime.getHour();
        Night night = blockerProperties.getNight();
        boolean isNight = currentHour >= night.getStartHour() || currentHour <= night.getEndHour();

        log.info("Night check: currentHour={}, nightStart={}, nightEnd={}, result={}",
                currentHour, night.getStartHour(), night.getEndHour(), isNight);

        return isNight;
    }

    private boolean isTooFrequent(String login, OperationType operationType) {
        String key = "op_count:" + login + ":" + operationType;
        Long count = opCountTemplate.opsForValue().increment(key);

        if (count == 1) {
            opCountTemplate.expire(key, blockerProperties.getTimeWindow());
            log.info("Initialized operation counter for key: {}, TTL: {}", key, blockerProperties.getTimeWindow());
        }

        boolean isTooFrequent = count > blockerProperties.getMaxOperations();
        log.info("Frequency check: key={}, count={}, max={}, result={}",
                key, count, blockerProperties.getMaxOperations(), isTooFrequent);

        return isTooFrequent;
    }

    private void saveSuspiciousOperation(OperationCheckRequest request, List<BlockReason> reasons) {
        SuspiciousOperation operation = SuspiciousOperation.builder()
                .login(request.getLogin())
                .amount(request.getAmount())
                .operationType(request.getOperationType())
                .timestamp(request.getTimestamp())
                .reasons(reasons.stream().map(Enum::name).collect(Collectors.joining(",")))
                .build();

        try {
            SuspiciousOperation saved = suspiciousOperationRepository.save(operation);
            log.info("Saved suspicious operation: {}", saved);
        } catch (Exception e) {
            log.error("Failed to save suspicious operation for login: {}", request.getLogin(), e);
        }
    }
}