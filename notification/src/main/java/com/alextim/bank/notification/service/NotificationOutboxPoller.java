package com.alextim.bank.notification.service;

import com.alextim.bank.notification.constant.NotificationStatus;
import com.alextim.bank.notification.entity.NotificationOutbox;
import com.alextim.bank.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.alextim.bank.notification.constant.NotificationStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxPoller {

    private final NotificationOutboxRepository outboxRepository;

    private final NotificationSenderServiceImpl notificationSender;

    @Transactional
    @Scheduled(fixedDelay = 10_000)
    public void processPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationStatus> pendingStatuses = List.of(PENDING, FAILED);

        List<NotificationOutbox> notifications = outboxRepository.findPendingNotifications(pendingStatuses, now);
        log.info("Found {} pending notifications", notifications.size());

        for (NotificationOutbox notification : notifications) {
            try {
                /* Защита от параллельной обработки*/
                int updated = outboxRepository.markAsProcessing(notification.getId(), PROCESSING);
                if (updated == 0) {
                    log.debug("Notification {} already in processing", notification.getId());
                    continue;
                }

                notificationSender.send(notification);

                outboxRepository.markAsSent(notification.getId(), SENT, LocalDateTime.now());
                log.info("Successfully sent notification: id={}", notification.getId());

            } catch (RuntimeException e) {
                log.error("Failed to send notification: id={}, aggregateLogin={}",
                        notification.getId(), notification.getAggregateLogin(), e);


                LocalDateTime nextRetry = calculateNextRetry(
                        notification.getRetryCount() + 1, notification.getMaxRetries());

                if (nextRetry == null) {
                    log.warn("Max retries reached for notification: id={}", notification.getId());

                    int ii = outboxRepository.markAsFailedPermanently(notification.getId(), FAILED_PERMANENTLY, e.getMessage());
                    System.out.println("i = " + ii);
                    log.warn("Notification {} marked as FAILED_PERMANENTLY", notification.getId());
                } else {
                    outboxRepository.incrementRetry(notification.getId(), nextRetry, e.getMessage());
                }
            }
        }
    }

    private LocalDateTime calculateNextRetry(int retryCount, int maxRetries) {
        if (retryCount >= maxRetries) {
            return null;
        }
        return switch (retryCount) {
            case 1 -> LocalDateTime.now().plusMinutes(1);
            case 2 -> LocalDateTime.now().plusMinutes(5);
            default -> LocalDateTime.now().plusMinutes(15);
        };
    }
}