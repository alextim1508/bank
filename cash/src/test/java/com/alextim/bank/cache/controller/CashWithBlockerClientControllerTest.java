package com.alextim.bank.cache.controller;


import com.alextim.bank.cache.service.CashMetricsService;
import com.alextim.bank.common.client.*;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.exchange.ConversionRequest;
import com.alextim.bank.common.dto.exchange.ConversionResponse;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CashWithBlockerClientControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountBalanceServiceClient accountBalanceServiceClient;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2TokenClient oAuth2TokenClient;
    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CashMetricsService cashMetricsService;

    @BeforeEach
    public void setUp() {
        when(notificationServiceClient.sendNotification(any()))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("OK"))));

        when(authServiceClient.checkTokenStatus("valid-token"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new TokenStatusResponse(true, false, false))));

        when(accountBalanceServiceClient.debitBalance(any()))
                .thenAnswer(invocation -> {
                    DebitBalanceRequest request = invocation.getArgument(0);
                    DebitBalanceResponse response = new DebitBalanceResponse(request.getLogin());
                    return ResponseEntity.ok(ApiResponse.success(response));
                });
        when(accountBalanceServiceClient.creditBalance(any()))
                .thenAnswer(invocation -> {
                    CreditBalanceRequest request = invocation.getArgument(0);
                    CreditBalanceResponse response = new CreditBalanceResponse(request.getLogin());
                    return ResponseEntity.ok(ApiResponse.success(response));
                });
        when(accountBalanceServiceClient.approve(any()))
                .thenAnswer(invocation -> {
                    ApproveOperationRequest request = invocation.getArgument(0);
                    ApproveOperationResponse response = new ApproveOperationResponse(request.getLogins());
                    return ResponseEntity.ok(ApiResponse.success(response));
                });

        when(exchangeServiceClient.convert(any()))
                .thenAnswer(invocation -> {
                    ConversionRequest request = invocation.getArgument(0);
                    BigDecimal convertedAmount;

                    if ("USD".equals(request.getSourceCurrency()) && "RUB".equals(request.getTargetCurrency())) {
                        convertedAmount = request.getAmount().multiply(BigDecimal.valueOf(92.5));
                    } else if ("RUB".equals(request.getSourceCurrency()) && "USD".equals(request.getTargetCurrency())) {
                        convertedAmount = request.getAmount().divide(BigDecimal.valueOf(92.5), 2, RoundingMode.HALF_UP);
                    } else {
                        convertedAmount = request.getAmount();
                    }

                    ConversionResponse response = ConversionResponse.builder()
                            .sourceCurrency(request.getSourceCurrency())
                            .targetCurrency(request.getTargetCurrency())
                            .amount(request.getAmount())
                            .convertedAmount(convertedAmount)
                            .exchangeRateToRub(92.5)
                            .build();

                    return ResponseEntity.ok(ApiResponse.success(response));
                });

        when(oAuth2TokenClient.getBearerToken(anyString(), anyString())).thenReturn("bearer");

        doNothing().when(cashMetricsService).incrementCashOperation(anyString(), anyString());
    }

    @Test
    void internalTransfer_ShouldUseFallback_WhenBlockerClientReturns500() throws Exception {
        mockMvc.perform(post("/cash/deposit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "ivan_ivanov",
                            "currency": "RUB",
                            "amount": 1000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist());

    }
}
