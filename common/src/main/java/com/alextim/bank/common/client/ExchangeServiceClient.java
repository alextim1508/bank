package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "exchange-service")
@CircuitBreaker(name = "exchangeService")
@Retry(name = "exchangeService")
public interface ExchangeServiceClient {

    @PostMapping("/convert")
    ResponseEntity<ApiResponse<ConversionResponse>> convert(@RequestBody ConversionRequest conversionRequest);

    @GetMapping("/rates")
    ResponseEntity<ApiResponse<List<RateResponseDto>>> getRates();

    @PutMapping("/rates")
    ResponseEntity<ApiResponse<UpdateRatesResponse>> updateRates(@RequestBody UpdateRatesRequest request);

    @PostMapping("/currency")
    ResponseEntity<ApiResponse<CurrencyResponse>> create(@RequestBody CurrencyRequest currencyRequest);

    @GetMapping("/currency")
    ResponseEntity<ApiResponse<List<CurrencyResponse>>> getAllCurrencies();
}
