package com.alextim.bank.exchange.controller;

import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.exchange.entity.Currency;
import com.alextim.bank.exchange.repository.CurrencyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CurrencyControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

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
    void create_ShouldReturnCreatedCurrency_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/exchange/currency")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "code": "USD",
                            "rusTitle": "Доллар США",
                            "title": "US Dollar",
                            "country": "United States",
                            "mark": "$"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.code").value("USD"))
                .andExpect(jsonPath("$.data.title").value("US Dollar"));
    }

    @Test
    void getAllCurrencies_ShouldReturnListOfCurrencies() throws Exception {
        currencyRepository.save(new Currency("USD", "US Dollar", "Доллар США", "United States", "$"));
        currencyRepository.save(new Currency("RUB", "Russian Ruble", "Рубль", "Russia", "₽"));

        mockMvc.perform(get("/exchange/currency")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.code == 'USD')].title").value("Доллар США"))
                .andExpect(jsonPath("$.data[?(@.code == 'RUB')].title").value("Рубль"));
    }

    @Test
    void create_ShouldReturnValidationError_WhenCodeInvalidSize() throws Exception {
        mockMvc.perform(post("/exchange/currency")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "code": "USDUSD",
                            "rusTitle": "Доллар США",
                            "title": "US Dollar",
                            "country": "United States",
                            "mark": "$"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.details").value("{code=[Currency code must be a valid ISO 4217 code (3 uppercase letters)]}"));
    }

    @Test
    void create_ShouldReturnValidationError_WhenRusTitleBlank() throws Exception {
        mockMvc.perform(post("/exchange/currency")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "code": "USD",
                            "rusTitle": "",
                            "title": "US Dollar",
                            "country": "United States",
                            "mark": "$"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.details").value("{rusTitle=[Russian title is required, Russian title must be between 2 and 50 characters]}"));
    }
}

