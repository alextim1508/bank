package com.alextim.bank.notification.constant;

/**
 * Статусы уведомления в системе.
 * Используются в таблицах notification_outbox и notification_delivery_log.
 */
public enum NotificationStatus {

     /**
      * Уведомление создано, но еще не обрабатывается.
      * Начальный статус при добавлении в outbox.
      */
     PENDING,

     /**
      * Уведомление в процессе обработки.
      * Устанавливается, чтобы избежать параллельной обработки.
      */
     PROCESSING,

     /**
      * Уведомление успешно отправлено (email, Telegram и т.д.).
      * Финальный статус для успешных сценариев.
      */
     SENT,

     /**
      * Отправка не удалась, но будут повторные попытки.
      * Используется, когда retryCount < maxRetries.
      */
     FAILED,

     /**
      * Отправка окончательно не удалась — достигнут лимит попыток.
      */
     FAILED_PERMANENTLY
}