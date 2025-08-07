package com.alextim.bank.notification.service;

import com.alextim.bank.notification.entity.NotificationOutbox;

public abstract class NotificationSenderService {
    public abstract void send(NotificationOutbox notification);
}
