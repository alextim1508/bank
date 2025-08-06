package com.alextim.bank.account.service;

import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.client.util.ExchangeClientUtils;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CachingCurrencyServiceImpl implements CurrencyService {

    private final ExchangeServiceClient exchangeServiceClient;

    @CacheEvict(value = "currencies", allEntries = true)
    public void refreshCurrenciesCache() {
        log.info("Currencies cache has been cleared");
    }

    @Override
    @Cacheable("currencies")
    public List<CurrencyResponse> getCurrencies() {
        log.info("Fetching currencies from Exchange service (cache miss or expired)");
        return ExchangeClientUtils.getCurrencies(exchangeServiceClient);
    }
}
