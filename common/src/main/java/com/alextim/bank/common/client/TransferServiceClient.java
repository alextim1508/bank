package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.ExternalTransferResponse;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "transfer-service")
@Retry(name = "transferService")
@CircuitBreaker(name = "transferService")
public interface TransferServiceClient {

    @PostMapping("/internal")
    ResponseEntity<ApiResponse<InternalTransferResponse>> makeInternalTransfer(@RequestBody InternalTransferRequest request);

    @PostMapping("/external")
    ResponseEntity<ApiResponse<ExternalTransferResponse>> makeExternalTransfer(@RequestBody ExternalTransferRequest request);
}
