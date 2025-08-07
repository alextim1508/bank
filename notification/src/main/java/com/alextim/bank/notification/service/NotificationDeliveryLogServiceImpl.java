package com.alextim.bank.notification.service;

import com.alextim.bank.notification.constant.NotificationChannel;
import com.alextim.bank.notification.constant.NotificationStatus;
import com.alextim.bank.notification.entity.NotificationDeliveryLog;
import com.alextim.bank.notification.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.alextim.bank.notification.constant.NotificationStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryLogServiceImpl implements NotificationDeliveryLogService {

    private final NotificationDeliveryLogRepository logRepository;

    public NotificationDeliveryLog logDelivery(Long outboxId,
                                               NotificationChannel channel,
                                               String recipient,
                                               NotificationStatus status,
                                               String errorMessage) {

        NotificationDeliveryLog logEntry = NotificationDeliveryLog.builder()
                .outboxId(outboxId)
                .channel(channel)
                .recipient(recipient)
                .sentAt(LocalDateTime.now())
                .status(status)
                .errorMessage(errorMessage)
                .build();

        NotificationDeliveryLog saved = logRepository.save(logEntry);
        log.info("Logged notification delivery: id={}", saved);

        return saved;
    }

    @Override
    public NotificationDeliveryLog logSuccess(Long outboxId,
                                              NotificationChannel channel,
                                              String recipient) {
        return logDelivery(outboxId, channel, recipient, SENT, null);
    }

    @Override
    public NotificationDeliveryLog logFailure(Long outboxId,
                                              NotificationChannel channel,
                                              String recipient,
                                              String errorMessage) {
        return logDelivery(outboxId, channel, recipient, FAILED, errorMessage);
    }

    @Override
    public List<NotificationDeliveryLog> getLogsByOutboxId(Long outboxId) {
        List<NotificationDeliveryLog> logs = logRepository.findByOutboxId(outboxId);
        log.debug("Found {} delivery logs for outboxId={}", logs.size(), outboxId);
        return logs;
    }

    @Override
    public List<NotificationDeliveryLog> getLogsByRecipient(String recipient) {
        return logRepository.findByRecipient(recipient);
    }

    @Override
    public void cleanupOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        log.info("Cleaning up delivery logs older than {}", cutoff);
        logRepository.deleteBySentAtBefore(cutoff);
        log.info("Cleanup completed");
    }
}