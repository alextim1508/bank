package com.alextim.bank.transfer.controller;


import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.ExternalTransferResponse;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferResponse;
import com.alextim.bank.transfer.exception.SuspiciousOperationException;
import com.alextim.bank.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/transfer")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/internal")
    public ResponseEntity<ApiResponse<InternalTransferResponse>> makeInternalTransfer(@Valid @RequestBody InternalTransferRequest request) {
        transferService.internalTransfer(request);

        InternalTransferResponse response = new InternalTransferResponse(request.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/external")
    public ResponseEntity<ApiResponse<ExternalTransferResponse>> makeExternalTransfer(@Valid @RequestBody ExternalTransferRequest request) {
        transferService.externalTransfer(request);

        ExternalTransferResponse response = ExternalTransferResponse.builder()
                .fromLogin(request.getFromLogin())
                .toLogin(request.getToLogin())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @ExceptionHandler(SuspiciousOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleSuspiciousOperationException(SuspiciousOperationException ex) {
        log.error("HandleSuspiciousOperationException", ex);

        ApiResponse<?> response = ApiResponse.error("Suspicious operation", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}