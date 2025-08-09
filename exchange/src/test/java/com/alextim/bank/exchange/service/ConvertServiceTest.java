package com.alextim.bank.exchange.service;


import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.exception.RateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ConvertServiceImpl.class})
@ActiveProfiles("test")
class ConvertServiceTest {

    @MockitoBean
    private CurrencyServiceImpl currencyService;

    @Autowired
    private ConvertServiceImpl exchangeService;

    private List<Currency> currencies;

    private Map<String, Double> rates;

    @BeforeEach
    void setUp() {
        currencies = Arrays.asList(
                new Currency("RUB", "Рубль", "Russian Ruble", "Россия", "₽"),
                new Currency( "USD", "Доллар", "US Dollar", "США", "$"),
                new Currency( "EUR", "Евро", "Euro", "Евро союз", "€"),
                new Currency( "CNY", "Юань", "Yuan Renminbi", "Китай", "¥")
        );

        rates = new HashMap<>();
        rates.put("USD", 90.0);
        rates.put("EUR", 100.0);
        rates.put("RUB", 1.0);
        rates.put("CNY", 108.0);
        rates.put("JPY", 0.5358);

        when(currencyService.getAllCurrencies()).thenReturn(currencies);
    }


    @Test
    void exchange_FromRUBToUSD_shouldReturnCorrectAmount() {
        exchangeService.updateRates(rates);

        BigDecimal result = exchangeService.convert("RUB", "USD", new BigDecimal("900"));

        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void exchange_FromUSDToRUB_shouldReturnCorrectAmount() {
        exchangeService.updateRates(rates);

        BigDecimal result = exchangeService.convert("USD", "RUB", new BigDecimal("10"));

        assertThat(result).isEqualByComparingTo("900.00");
    }

    @Test
    void exchange_FromUSDtoEUR_shouldReturnCorrectAmount() {
        exchangeService.updateRates(rates);

        BigDecimal result = exchangeService.convert("USD", "EUR", new BigDecimal("2.34"));

        // 2.34 USD = 210.6 RUB, 210.6 RUB / 100 = 2.106 EUR
        assertThat(result).isEqualByComparingTo("2.11");
    }

    @Test
    void exchange_SameCurrency_shouldReturnSameAmount() {
        BigDecimal result = exchangeService.convert("USD", "USD", new BigDecimal("100"));

        assertThat(result).isEqualByComparingTo("100.00");
    }

    @Test
    void exchange_SourceRateNotFound_ShouldThrowException() {
        exchangeService.updateRates(rates);

        assertThatThrownBy(() -> exchangeService.convert("MYR", "RUB", BigDecimal.TEN))
                .isInstanceOf(RateNotFoundException.class)
                .hasMessageContaining("MYR not found");
    }

    @Test
    void updateRates_shouldUpdateAllRates() {
        exchangeService.updateRates(rates);

        assertThat(exchangeService.convert("USD", "RUB", BigDecimal.ONE))
                .isEqualByComparingTo("90.00");
        assertThat(exchangeService.convert("EUR", "RUB", BigDecimal.ONE))
                .isEqualByComparingTo("100.00");
        assertThat(exchangeService.convert("RUB", "CNY", BigDecimal.valueOf(12.5)))
                .isEqualByComparingTo("0.12");
    }

    @Test
    void updateRates_shouldAlwaysSetRUBToOne() {
        Map<String, Double> customRates = new HashMap<>(rates);
        customRates.put("RUB", 2.0);

        exchangeService.updateRates(customRates);

        BigDecimal result = exchangeService.convert("RUB", "USD", new BigDecimal("90"));
        assertThat(result).isEqualByComparingTo("1.00");
    }
}