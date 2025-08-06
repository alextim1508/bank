package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.RateResponseDto;
import com.alextim.bank.common.dto.exchange.UpdateRatesRequest;
import com.alextim.bank.common.dto.exchange.UpdateRatesResponse;
import com.alextim.bank.exchange.exception.RateNotFoundException;
import com.alextim.bank.exchange.service.ConvertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/exchange/rates")
@RequiredArgsConstructor
@Slf4j
public class RateController {

    private final ConvertService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RateResponseDto>>> getRates() {
        log.info("Incoming request to get all currency rates");

        List<RateResponseDto> allRates = service.getRates();
        log.info("Returning {} rate entries", allRates.size());
        log.debug("Full rates payload: {}", allRates);

        return ResponseEntity.ok(ApiResponse.success(allRates));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UpdateRatesResponse>> updateRates(@Valid @RequestBody UpdateRatesRequest request) {
        log.info("Incoming request to update rates: {}", request);

        service.updateRates(request.getRates());
        log.info("Successfully updated {} rate(s)", request.getRates().size());

        return ResponseEntity.ok(ApiResponse.success(new UpdateRatesResponse()));
    }

    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleRateNotFoundException(RateNotFoundException ex) {
        log.warn("Rate not found: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Unknown currency code", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
