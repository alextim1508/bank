package com.alextim.bank.notification.service;

import com.alextim.bank.notification.entity.NotificationOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.alextim.bank.notification.constant.NotificationChannel.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderServiceImpl extends NotificationSenderService {

    private final NotificationDeliveryLogService deliveryLogService;

    private final AccountContactService accountContactService;

    @Override
    public void send(NotificationOutbox notification) {
        String aggregateLogin = notification.getAggregateLogin();
        log.info("Sending notification ID={} for login='{}'", notification.getId(), aggregateLogin);

        Map<String, String> contacts = accountContactService.getContacts(aggregateLogin);
        if (contacts.isEmpty()) {
            log.warn("No contacts found for login='{}', skipping notification ID={}", aggregateLogin, notification.getId());
            return;
        }

        String message = notification.getPayload();

        sendViaEmail(contacts, message, notification);
        sendViaSms(contacts, message, notification);
        sendViaTelegram(contacts, message, notification);
    }

    private void sendViaEmail(Map<String, String> contacts, String message, NotificationOutbox notification) {
        String email = contacts.get(EMAIL.name());
        if (email == null)
            return;

        try {
            sendEmail(email, message);
            deliveryLogService.logSuccess(notification.getId(), EMAIL, email);
            log.info("Email sent to '{}', notification ID={}", email, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email to '{}', notification ID={}", email, notification.getId(), e);
            deliveryLogService.logFailure(notification.getId(), EMAIL, email, e.getMessage());
        }
    }

    private void sendViaSms(Map<String, String> contacts, String message, NotificationOutbox notification) {
        String phone = contacts.get(PHONE.name());
        if (phone == null)
            return;

        try {
            sendSms(phone, message);
            deliveryLogService.logSuccess(notification.getId(), PHONE, phone);
            log.info("SMS sent to '{}', notification ID={}", phone, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send SMS to '{}', notification ID={}", phone, notification.getId(), e);
            deliveryLogService.logFailure(notification.getId(), PHONE, phone, e.getMessage());
        }
    }

    private void sendViaTelegram(Map<String, String> contacts, String message, NotificationOutbox notification) {
        String tg = contacts.get(TELEGRAM.name());
        if (tg == null)
            return;

        try {
            sendTelegramMessage(tg, message);
            deliveryLogService.logSuccess(notification.getId(), TELEGRAM, tg);
            log.info("Telegram message sent to '{}', notification ID={}", tg, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send Telegram message to '{}', notification ID={}", tg, notification.getId(), e);
            deliveryLogService.logFailure(notification.getId(), TELEGRAM, tg, e.getMessage());
        }
    }

    private void sendEmail(String to, String message) {
        log.warn("Email sending not implemented: to='{}', message='{}'", to, message);
    }

    private void sendTelegramMessage(String chatId, String message) {
        log.warn("Telegram sending not implemented: chatId='{}', message='{}'", chatId, message);
    }

    private void sendSms(String phone, String message) {
        log.warn("SMS sending not implemented: phone='{}', message='{}'", phone, message);
    }
}