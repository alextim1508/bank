package com.alextim.bank.exchange.service;


import com.alextim.bank.common.dto.exchange.RateResponseDto;
import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.exception.RateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvertServiceImpl implements ConvertService {

    private final Map<String, Double> currencyRates = new ConcurrentHashMap<>();

    private final CurrencyService currencyService;

    @Override
    public BigDecimal convert(String sourceCurrency, String targetCurrency, BigDecimal amount) {
        log.info("sourceCurrency: {}, targetCurrency: {}, amount: {}", sourceCurrency, targetCurrency, amount);

        if (sourceCurrency.equals(targetCurrency)) {
            log.info("Currencies are the same");
            return amount;
        }

        if (sourceCurrency.equals("RUB")) {
            log.info("Convert from RUB");
            Double rate = currencyRates.get(targetCurrency);
            log.info("Rate: {}", rate);
            if (rate == null) {
                throw new RateNotFoundException(targetCurrency);
            }

            BigDecimal convertedAmount = amount.divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
            log.info("Converted amount: {}", convertedAmount);

            return convertedAmount;
        }

        if (targetCurrency.equals("RUB")) {
            log.info("Convert to RUB");

            Double rate = currencyRates.get(sourceCurrency);
            if (rate == null) {
                throw new RateNotFoundException(sourceCurrency);
            }
            log.info("Rate: {}", rate);

            BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
            log.info("Converted amount: {}", convertedAmount);

            return convertedAmount;
        }

        log.info("Convert source -> RUB -> target");
        Double sourceRate = currencyRates.get(sourceCurrency);
        log.info("Source rate: {}", sourceRate);

        if (sourceRate == null) {
            throw new RateNotFoundException(sourceCurrency);
        }

        BigDecimal amountInRUB = amount.multiply(BigDecimal.valueOf(sourceRate));
        log.info("Amount in RUB: {}", amountInRUB);

        Double targetRate = currencyRates.get(targetCurrency);
        log.info("Target rate: {}", targetRate);

        if (targetRate == null) {
            throw new RateNotFoundException(targetCurrency);
        }

        BigDecimal convertedAmount = amountInRUB.divide(BigDecimal.valueOf(targetRate), 2, RoundingMode.HALF_UP);
        log.info("Converted amount: {}", convertedAmount);

        return convertedAmount;
    }

    @Override
    public void updateRates(Map<String, Double> newRates) {
        log.info("Updating {} currency rates", newRates.size());
        log.debug("New rates: {}", newRates);

        List<Currency> currencies = currencyService.getAllCurrencies();
        log.debug("Retrieved {} currencies from service", currencies.size());

        Map<String, Currency> currencyMap = currencies.stream()
                .collect(Collectors.toMap(Currency::getCode, Function.identity()));

        newRates.forEach((currency, rate) -> {
            if (currencyMap.containsKey(currency))
                currencyRates.putAll(newRates);
        });


        currencyRates.put("RUB", 1.0);

        log.info("Successfully updated {} rates. RUB rate fixed to 1.0", currencyRates.size() - 1);
    }

    @Override
    public List<RateResponseDto> getRates() {
        log.info("Fetching all currency rates");

        List<Currency> currencies = currencyService.getAllCurrencies();
        log.debug("Retrieved {} currencies from service", currencies.size());

        Map<String, Currency> currencyMap = currencies.stream()
                .collect(Collectors.toMap(Currency::getCode, Function.identity()));

        List<RateResponseDto> rates = currencyRates.entrySet().stream()
                .filter(entry -> currencyMap.containsKey(entry.getKey()))
                .map(entry -> {
                    Currency currency = currencyMap.get(entry.getKey());
                    return new RateResponseDto(
                            entry.getKey(),
                            currency.getTitle(),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(RateResponseDto::getCode))
                .collect(Collectors.toList());

        log.info("Returning {} rate entries", rates.size());
        log.debug("Full rates payload: {}", rates);

        return rates;
    }
}
