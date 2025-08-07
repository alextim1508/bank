package com.alextim.bank.notification.repository;

import com.alextim.bank.notification.constant.NotificationChannel;
import com.alextim.bank.notification.constant.NotificationStatus;
import com.alextim.bank.notification.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    List<NotificationDeliveryLog> findByOutboxId(Long outboxId);

    List<NotificationDeliveryLog> findByRecipient(String recipient);

    List<NotificationDeliveryLog> findByChannel(NotificationChannel channel);

    List<NotificationDeliveryLog> findByStatus(NotificationStatus status);

    long countByOutboxIdAndStatus(Long outboxId, NotificationStatus status);

    void deleteBySentAtBefore(LocalDateTime cutoffDate);
}