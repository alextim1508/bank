package com.alextim.bank.common.client;


import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-auth-service")
public interface AuthServiceClient {

    @PostMapping("/login")
    ResponseEntity<ApiResponse<TokenPairResponse>> login(@RequestBody LoginRequest request);

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<TokenPairResponse>> refresh(@RequestBody RefreshRequest request);

    @GetMapping("/check")
    ResponseEntity<ApiResponse<TokenStatusResponse>> checkTokenStatus(@RequestParam String token);
}

