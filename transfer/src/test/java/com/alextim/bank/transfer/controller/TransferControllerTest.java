package com.alextim.bank.transfer.controller;

import com.alextim.bank.common.client.*;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import com.alextim.bank.common.dto.exchange.ConversionRequest;
import com.alextim.bank.common.dto.exchange.ConversionResponse;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.transfer.repository.TransferOperationRepository;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransferControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferOperationRepository transferOperationRepository;

    @MockitoBean
    private AccountBalanceServiceClient accountBalanceServiceClient;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    @MockitoBean
    private BlockerServiceClient blockerServiceClient;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

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

        when(blockerServiceClient.checkOperation(any()))
                .thenAnswer(invocation -> {
                    OperationCheckRequest request = invocation.getArgument(0);
                    if(request.getLogin().equals("blocked_user")) {
                        OperationCheckResponse response = new OperationCheckResponse(request.getLogin(), false);
                        return ResponseEntity.ok(ApiResponse.success(response));
                    }

                    OperationCheckResponse response = new OperationCheckResponse(request.getLogin(), true);
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
    }

    @Test
    void makeInternalTransfer_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/transfer/internal")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "ivan_ivanov",
                            "fromCurrency": "USD",
                            "toCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"));
    }

    @Test
    void makeExternalTransfer_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/transfer/external")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "fromLogin": "ivan_ivanov",
                            "fromCurrency": "USD",
                            "toLogin": "kirill_kirillov",
                            "toCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.toLogin").value("kirill_kirillov"))
                .andExpect(jsonPath("$.data.fromLogin").value("ivan_ivanov")) ;
    }

    @Test
    void makeInternalTransfer_ShouldReturnValidationError_WhenLoginBlank() throws Exception {
        mockMvc.perform(post("/transfer/internal")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "",
                            "fromCurrency": "USD",
                            "toCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{login=[Sender login is required, Sender login must be between 4 and 20 characters]}"));
    }

    @Test
    void makeInternalTransfer_ShouldReturnValidationError_WhenAmountNegative() throws Exception {
        mockMvc.perform(post("/transfer/internal")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "ivan",
                            "fromCurrency": "USD",
                            "toCurrency": "RUB",
                            "amount": -100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{amount=[Amount must be greater than zero]}"));
    }

    @Test
    void makeInternalTransfer_ShouldReturnBadRequest_WhenSuspiciousOperation() throws Exception {
        mockMvc.perform(post("/transfer/internal")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "login": "blocked_user",
                            "fromCurrency": "USD",
                            "toCurrency": "RUB",
                            "amount": 100
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Suspicious operation"))
                .andExpect(jsonPath("$.error.details").value("Suspicious operation"));
    }
}


