package com.alextim.bank.notification.controller;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.notification.service.NotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationOutboxService notificationOutboxService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@RequestBody NotificationRequest request) {
        log.info("Incoming request for creating notification: {}", request);

        notificationOutboxService.save(request);

        return ResponseEntity.ok(ApiResponse.success(new NotificationResponse(request.getLogin())));
    }
}
