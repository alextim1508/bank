package com.alextim.bank.notification.service;

import com.alextim.bank.notification.entity.NotificationOutbox;
import com.alextim.bank.notification.repository.NotificationOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.alextim.bank.common.constant.AggregateType.ACCOUNT;
import static com.alextim.bank.common.constant.EventType.ACCOUNT_CREATED;
import static com.alextim.bank.notification.constant.NotificationStatus.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class NotificationOutboxPollerTest extends AbstractControllerTestContainer{

    @Autowired
    private NotificationOutboxPoller poller;

    @Autowired
    private NotificationOutboxRepository outboxRepository;

    @MockitoBean
    private NotificationSenderServiceImpl notificationSender;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
    }


    @Test
    void processPendingNotifications_ShouldMarkAsSent_WhenSendSuccess() {
        NotificationOutbox notification = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("ivan_ivanov")
                .eventType(ACCOUNT_CREATED)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .payload("Account has been created")
                .build();
        outboxRepository.save(notification);

        doNothing().when(notificationSender).send(any(NotificationOutbox.class));

        poller.processPendingNotifications();


        Optional<NotificationOutbox> updated = outboxRepository.findById(notification.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(SENT);
        assertThat(updated.get().getProcessedAt()).isNotNull();
    }

    @Test
    void processPendingNotifications_ShouldIncrementRetry_WhenSendFails() {
        NotificationOutbox notification = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("ivan_ivanov")
                .eventType(ACCOUNT_CREATED)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .payload("Account has been created")
                .retryCount(1)
                .status(FAILED)
                .build();
        outboxRepository.save(notification);


        doThrow(new RuntimeException("Network error"))
                .when(notificationSender).send(any());

        poller.processPendingNotifications();


        Optional<NotificationOutbox> updated = outboxRepository.findById(notification.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(FAILED);
        assertThat(updated.get().getRetryCount()).isEqualTo(2);
        assertThat(updated.get().getNextRetryAt()).isAfter(notification.getNextRetryAt());
        assertThat(updated.get().getLastError()).contains("Network error");
    }

    @Test
    void processPendingNotifications_ShouldSkipIfAlreadyProcessing() {
        NotificationOutbox notification = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("ivan_ivanov")
                .eventType(ACCOUNT_CREATED)
                .status(PROCESSING)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .payload("Account has been created")
                .build();

        outboxRepository.save(notification);

        doNothing().when(notificationSender).send(any(NotificationOutbox.class));

        poller.processPendingNotifications();

        verify(notificationSender, never()).send(any());
    }

    @Test @Transactional
    void processPendingNotifications_ShouldMarkAsFailedPermanently_WhenMaxRetriesReached() {
        NotificationOutbox notification = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("ivan_ivanov")
                .eventType(ACCOUNT_CREATED)
                .status(FAILED)
                .retryCount(2)
                .maxRetries(3)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .payload("Account has been created")
                .build();

        outboxRepository.save(notification);

        doThrow(new RuntimeException("Service unavailable"))
                .when(notificationSender).send(notification);


        poller.processPendingNotifications();


        Optional<NotificationOutbox> updated = outboxRepository.findById(notification.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(FAILED_PERMANENTLY);
        assertThat(updated.get().getLastError()).contains("Service unavailable");

        verify(notificationSender).send(notification);
    }

    @Test
    void processPendingNotifications_ShouldNotProcess_WhenNextRetryAtInFuture() {
        NotificationOutbox notification = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("ivan_ivanov")
                .eventType(ACCOUNT_CREATED)
                .status(PENDING)
                .nextRetryAt(LocalDateTime.now().plusMinutes(1))
                .payload("Account has been created")
                .build();

        outboxRepository.save(notification);

        doNothing().when(notificationSender).send(any(NotificationOutbox.class));

        poller.processPendingNotifications();

        verify(notificationSender, never()).send(any());
    }

    @Test
    void processPendingNotifications_ShouldProcessOnlyPendingAndFailed() {
        NotificationOutbox pending = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("user1")
                .eventType(ACCOUNT_CREATED)
                .payload("{}")
                .status(PENDING)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .build();

        NotificationOutbox failed = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("user2")
                .eventType(ACCOUNT_CREATED)
                .payload("{}")
                .status(FAILED)
                .retryCount(1)
                .maxRetries(3)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .build();

        NotificationOutbox sent = NotificationOutbox.builder()
                .aggregateType(ACCOUNT)
                .aggregateLogin("user3")
                .eventType(ACCOUNT_CREATED)
                .payload("{}")
                .status(SENT)
                .nextRetryAt(LocalDateTime.now().minusSeconds(1))
                .build();

        outboxRepository.saveAll(List.of(pending, failed, sent));

        doAnswer(invocation -> {
            NotificationOutbox n = invocation.getArgument(0);
            if (n.getAggregateLogin().equals("user2")) {
                throw new RuntimeException("Retryable error");
            }
            return invocation;
        }).when(notificationSender).send(any());


        poller.processPendingNotifications();

        assertThat(outboxRepository.findById(pending.getId()).get().getStatus()).isEqualTo(SENT);
        assertThat(outboxRepository.findById(failed.getId()).get().getStatus()).isEqualTo(FAILED);
        assertThat(outboxRepository.findById(sent.getId()).get().getStatus()).isEqualTo(SENT);
    }
}