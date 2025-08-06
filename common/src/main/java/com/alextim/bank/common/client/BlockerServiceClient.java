package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "blocker-service")
public interface BlockerServiceClient {

    @PostMapping("/check-operation")
    ResponseEntity<ApiResponse<OperationCheckResponse>> checkOperation(@RequestBody OperationCheckRequest request);
}