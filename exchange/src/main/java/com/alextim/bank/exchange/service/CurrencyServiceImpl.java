package com.alextim.bank.exchange.service;

import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @CacheEvict(value = "currencies", allEntries = true)
    @Override
    public Currency create(Currency currency) {
        log.info("Creating new currency: code={}, name={}", currency.getCode(), currency.getTitle());

        Currency saved = currencyRepository.save(currency);

        log.info("Successfully created currency with code: {}", saved.getCode());
        return saved;
    }

    @Cacheable(value = "currencies", sync = true)
    @Override
    public List<Currency> getAllCurrencies() {
        log.debug("Fetching all currencies from database (cache miss or expired)");

        List<Currency> currencies = currencyRepository.findAll();

        log.info("Retrieved {} currencies from database", currencies.size());
        log.debug("Currencies: {}", currencies);

        return currencies;
    }
}
