package com.alextim.bank.notification.service;

import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.notification.entity.NotificationOutbox;

public interface NotificationOutboxService {

    NotificationOutbox save(NotificationRequest request);
}
