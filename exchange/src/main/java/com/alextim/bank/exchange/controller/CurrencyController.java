package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.CurrencyRequest;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.mapper.CurrencyMapper;
import com.alextim.bank.exchange.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/exchange/currency")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final CurrencyMapper currencyMapper;

    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CurrencyResponse>> create(@Valid @RequestBody CurrencyRequest request) {
        log.info("Incoming request to create currency: {}", request);

        Currency currency = currencyMapper.toEntity(request);
        log.debug("Mapped CurrencyRequest to entity: {}", currency);

        Currency savedCurrency = currencyService.create(currency);
        log.info("Successfully created currency with code: {}", savedCurrency.getCode());

        CurrencyResponse response = currencyMapper.toDto(savedCurrency);
        log.debug("Mapped saved currency to response: {}", response);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        log.debug("Retrieved {} currencies from service", currencies.size());

        List<CurrencyResponse> response = currencies.stream()
                .map(currencyMapper::toDto)
                .toList();

        log.info("Returning {} currency responses", response.size());
        log.debug("Full response payload: {}", response);

        return ResponseEntity.ok(ApiResponse.success(response));
    }


}
