package com.alextim.bank.exchange.service;

import com.alextim.bank.common.dto.exchange.RateResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ConvertService {

    BigDecimal convert(String sourceCurrency, String targetCurrency, BigDecimal amount);

    void updateRates(Map<String, Double> newRates);

    List<RateResponseDto> getRates();
}
