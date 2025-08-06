package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.ConversionRequest;
import com.alextim.bank.common.dto.exchange.ConversionResponse;
import com.alextim.bank.exchange.exception.RateNotFoundException;
import com.alextim.bank.exchange.service.ConvertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping(value = "/exchange/convert")
@RequiredArgsConstructor
@Slf4j
public class ConvertController {

    private final ConvertService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversionResponse>> convert(@Valid @RequestBody ConversionRequest request) {
        log.info("Incoming request for converting {}", request);

        String sourceCurrency = request.getSourceCurrency();
        String targetCurrency = request.getTargetCurrency();

        BigDecimal convertedAmount = service.convert(sourceCurrency, targetCurrency, request.getAmount());
        log.info("Converted amount {}", convertedAmount);

        ConversionResponse response = ConversionResponse.builder()
                .convertedAmount(convertedAmount)
                .sourceCurrency(sourceCurrency)
                .targetCurrency(targetCurrency)
                .build();
        log.info("Response: {}", response);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleRateNotFoundException(RateNotFoundException ex) {
        log.error("handleRateNotFoundException", ex);

        ApiResponse<?> response = ApiResponse.error("Unknown currency code", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
