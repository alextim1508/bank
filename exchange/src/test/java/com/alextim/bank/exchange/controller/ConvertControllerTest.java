package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.common.client.OAuth2TokenClient;
import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.repository.CurrencyRepository;
import com.alextim.bank.exchange.service.ConvertService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConvertControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ConvertService convertService;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2TokenClient oauth2TokenClient;

    @AfterEach
    public void tearDown() {
        currencyRepository.deleteAll();
    }

    @Test
    void convert_ShouldReturnConvertedAmount_WhenValidRequest() throws Exception {
        currencyRepository.save(new Currency("EUR", "Евро", "Euro", "Евро союз", "€"));
        currencyRepository.save(new Currency("USD", "US Dollar", "Доллар США", "United States", "$"));
        currencyRepository.save(new Currency("RUB", "Russian Ruble", "Рубль", "Russia", "₽"));

        convertService.updateRates(Map.of("USD", 100., "EUR", 120.));

        mockMvc.perform(post("/exchange/convert")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceCurrency": "USD",
                            "targetCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.data.targetCurrency").value("RUB"))
                .andExpect(jsonPath("$.data.convertedAmount").value("10000"));
    }

    @Test
    void convert_ShouldReturnValidationError_WhenSourceCurrencyBlank() throws Exception {
        mockMvc.perform(post("/exchange/convert")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceCurrency": "",
                            "targetCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details").value("{sourceCurrency=[Currency code must be a valid ISO 4217 code (3 uppercase letters), {conversion.sourceCurrency.notblank}]}"));
    }

    @Test
    void convert_ShouldReturnValidationError_WhenTargetCurrencyInvalidSize() throws Exception {
        mockMvc.perform(post("/exchange/convert")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceCurrency": "USD",
                            "targetCurrency": "RU",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details").value("{targetCurrency=[Currency code must be a valid ISO 4217 code (3 uppercase letters)]}"));
    }

    @Test
    void convert_ShouldReturnValidationError_WhenAmountNegative() throws Exception {
        mockMvc.perform(post("/exchange/convert")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceCurrency": "USD",
                            "targetCurrency": "RUB",
                            "amount": -100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details").value("{amount=[{conversion.amount.positive}]}"));
    }

    @Test
    void convert_ShouldReturnBadRequest_WhenRateNotFound() throws Exception {
        currencyRepository.save(new Currency("EUR", "Евро", "Euro", "Евро союз", "€"));
        currencyRepository.save(new Currency("USD", "US Dollar", "Доллар США", "United States", "$"));
        currencyRepository.save(new Currency("RUB", "Russian Ruble", "Рубль", "Russia", "₽"));

        convertService.updateRates(Map.of("USD", 100., "EUR", 120.));

        mockMvc.perform(post("/exchange/convert")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "sourceCurrency": "USD",
                            "targetCurrency": "XYZ",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Unknown currency code"))
                .andExpect(jsonPath("$.error.details").value("XYZ not found"));
    }
}
