package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    Logger log = LoggerFactory.getLogger(BlockerServiceClient.class);

    @PostMapping
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendNotification")
    @Retry(name = "notificationService")
    ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(@RequestBody NotificationRequest dto);

    default ResponseEntity<ApiResponse<NotificationResponse>> fallbackSendNotification(
            NotificationRequest request,
            Throwable throwable) {
        log.warn("Fallback triggered for notification-service sendNotification. Reason: {}", throwable.getMessage());

        NotificationResponse response = new NotificationResponse("stub login");
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
