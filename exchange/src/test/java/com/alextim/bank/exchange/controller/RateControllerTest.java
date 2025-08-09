package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.repository.CurrencyRepository;
import com.alextim.bank.exchange.service.ConvertService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RateControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConvertService convertService;

    @Autowired
    private CurrencyRepository currencyRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @AfterEach
    public void tearDown() {
        currencyRepository.deleteAll();
    }

    @Test
    void getRatesByCurrencyCodes_ShouldReturnAllRates() throws Exception {
        currencyRepository.save(new Currency("USD", "US Dollar", "Доллар США", "United States", "$"));
        currencyRepository.save(new Currency("RUB", "Russian Ruble", "Рубль", "Russia", "₽"));

        convertService.updateRates(Map.of("USD", 100., "RUB", 1.));

        mockMvc.perform(get("/exchange/rates")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.code == 'USD')].value").value(100.))
                .andExpect(jsonPath("$.data[?(@.code == 'RUB')].value").value(1.));
    }

    @Test
    void updateRates_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        currencyRepository.save(new Currency("USD", "US Dollar", "Доллар США", "United States", "$"));
        currencyRepository.save(new Currency("RUB", "Russian Ruble", "Рубль", "Russia", "₽"));

        mockMvc.perform(put("/exchange/rates")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rates": {
                                       "USD": 93.0 ,
                                       "EUR": 101.0
                                    }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }
}
