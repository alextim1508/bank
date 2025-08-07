package com.alextim.bank.notification.repository;

import com.alextim.bank.notification.constant.NotificationStatus;
import com.alextim.bank.notification.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("SELECT o FROM NotificationOutbox o " +
            "WHERE o.status IN :pendingStatuses " +
            "AND o.nextRetryAt <= :currentTime " +
            "AND o.retryCount < o.maxRetries")
    List<NotificationOutbox> findPendingNotifications(
            @Param("pendingStatuses") List<NotificationStatus> pendingStatuses,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationOutbox o " +
            "SET o.status = :status " +
            "WHERE o.id = :id AND o.status IN ('PENDING', 'FAILED')")
    int markAsProcessing(
            @Param("id") Long id,
            @Param("status") NotificationStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationOutbox o " +
            "SET o.status = :status, o.processedAt = :processedAt " +
            "WHERE o.id = :id AND o.status = 'PROCESSING'")
    int markAsSent(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("processedAt") LocalDateTime processedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationOutbox o " +
            "SET o.status = :status, " +
            "o.lastError = :error, " +
            "o.nextRetryAt = NULL " +
            "WHERE o.id = :id AND o.status <> 'SENT'")
    int markAsFailedPermanently(
            @Param("id") Long id,
            @Param("status") NotificationStatus status,
            @Param("error") String error
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationOutbox o " +
            "SET o.retryCount = o.retryCount + 1, " +
            "o.nextRetryAt = :nextRetryAt, " +
            "o.lastError = :error, " +
            "o.status = 'FAILED' " +
            "WHERE o.id = :id AND o.retryCount < o.maxRetries")
    int incrementRetry(
            @Param("id") Long id,
            @Param("nextRetryAt") LocalDateTime nextRetryAt,
            @Param("error") String error
    );
}