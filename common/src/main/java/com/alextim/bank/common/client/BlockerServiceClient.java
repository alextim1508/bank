package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "blocker-service")
public interface BlockerServiceClient {

    Logger log = LoggerFactory.getLogger(BlockerServiceClient.class);

    @PostMapping("/check-operation")
    @CircuitBreaker(name = "blockerService", fallbackMethod = "fallbackCheckOperation")
    @Retry(name = "blockerService")
    ResponseEntity<ApiResponse<OperationCheckResponse>> checkOperation(@RequestBody OperationCheckRequest request);

    default ResponseEntity<ApiResponse<OperationCheckResponse>> fallbackCheckOperation(
            OperationCheckRequest request,
            Throwable throwable) {
        log.warn("Fallback triggered for blocker-service checkOperation. Reason: {}", throwable.getMessage());

        OperationCheckResponse response = new OperationCheckResponse("stub login", true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}