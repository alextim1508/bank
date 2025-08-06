package com.alextim.bank.blocker.controller;

import com.alextim.bank.blocker.service.BlockerService;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blocker")
@RequiredArgsConstructor
@Slf4j
public class BlockerController {

    private final BlockerService blockerService;

    @PostMapping("/check-operation")
    public ResponseEntity<ApiResponse<OperationCheckResponse>> checkOperation(@Valid @RequestBody OperationCheckRequest request) {
        log.info("Incoming request to check operation: {}", request);

        boolean isSuspicious = blockerService.isSuspicious(request);
        log.info("Operation check completed: {}", isSuspicious);

        OperationCheckResponse response = OperationCheckResponse.builder()
                .login(request.getLogin())
                .approved(!isSuspicious)
                .build();
        log.debug("Returning response: {}", response);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}