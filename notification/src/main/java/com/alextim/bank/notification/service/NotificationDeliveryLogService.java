package com.alextim.bank.notification.service;

import com.alextim.bank.notification.constant.NotificationChannel;
import com.alextim.bank.notification.entity.NotificationDeliveryLog;

import java.util.List;

public interface NotificationDeliveryLogService {
    NotificationDeliveryLog logSuccess(Long outboxId,
                                       NotificationChannel channel,
                                       String recipient);

    NotificationDeliveryLog logFailure(Long outboxId,
                                       NotificationChannel channel,
                                       String recipient,
                                       String errorMessage);

    List<NotificationDeliveryLog> getLogsByOutboxId(Long outboxId);

    List<NotificationDeliveryLog> getLogsByRecipient(String recipient);

    void cleanupOldLogs(int days);
}
