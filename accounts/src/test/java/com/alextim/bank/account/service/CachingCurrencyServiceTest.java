package com.alextim.bank.account.service;


import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;


@ImportAutoConfiguration({
        CacheAutoConfiguration.class
})
@EnableCaching
@SpringBootTest(classes = {CachingCurrencyServiceImpl.class, CacheManager.class})
@ActiveProfiles("test")
public class CachingCurrencyServiceTest {

    @Autowired
    private CurrencyService currencyService;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    private List<CurrencyResponse> currencyResponses;

    @BeforeEach
    void setUp() {
        currencyResponses = Arrays.asList(
                new CurrencyResponse("Рубль", "RUB"),
                new CurrencyResponse("Доллар", "USD"),
                new CurrencyResponse("Евро", "EUR")
        );

        when(exchangeServiceClient.getAllCurrencies())
                .thenReturn(ResponseEntity.ok(ApiResponse.success(currencyResponses)));
    }


    @Test
    void getCurrencies_SecondCall_ShouldNotCallClient() {
        currencyService.getCurrencies();
        currencyService.getCurrencies();

        verify(exchangeServiceClient, times(1)).getAllCurrencies();
    }

}