package com.alextim.bank.notification.service;

import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.notification.entity.NotificationOutbox;
import com.alextim.bank.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxServiceImpl implements NotificationOutboxService {

    private final NotificationOutboxRepository outboxRepository;

    @Override
    public NotificationOutbox save(NotificationRequest request) {
        log.info("Saving notification to outbox: {}", request);

        NotificationOutbox notificationOutbox = NotificationOutbox.builder()
                .aggregateType(request.getAggregateType())
                .eventType(request.getEventType())
                .aggregateLogin(request.getLogin())
                .payload(request.getMessage())
                .build();

        NotificationOutbox saved = outboxRepository.save(notificationOutbox);

        log.info("Notification saved: {}", saved);
        return saved;
    }

}
