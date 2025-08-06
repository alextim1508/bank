package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.common.exception.AccountBalanceServiceClientException;
import org.springframework.http.ResponseEntity;

public class NotificationClientUtils {

    public static NotificationResponse sendNotification(NotificationServiceClient notificationServiceClient,
                                                        NotificationRequest request) {

        ResponseEntity<ApiResponse<NotificationResponse>> response = notificationServiceClient.sendNotification(request);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountBalanceServiceClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
