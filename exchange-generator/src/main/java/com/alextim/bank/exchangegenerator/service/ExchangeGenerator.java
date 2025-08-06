package com.alextim.bank.exchangegenerator.service;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.UpdateRatesRequest;
import com.alextim.bank.common.dto.exchange.UpdateRatesResponse;
import com.alextim.bank.exchangegenerator.dto.OAuthTokenResponse;
import com.alextim.bank.exchangegenerator.property.OAuth2ClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeGenerator {

    private final RestTemplate restTemplate;
    private final OAuth2ClientProperties clientProperties;
    private final Random random = new Random();

    @Value("${spring.cloud.openfeign.client.config.exchange-service.url}")
    private String exchangeServiceUrl;

    public Map<String, Double> generateRates() {
        Map<String, Double> rates = new HashMap<>();

        rates.put("RUB", 1.0);

        double usdRate = 60 + (100 - 60) * random.nextDouble();
        rates.put("USD", usdRate);

        double eurRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("EUR", eurRate);

        double cnyRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("CNY", cnyRate);

        double gbpRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("GBP", gbpRate);

        return rates;
    }

    @Scheduled(fixedRate = 5_000)
    public void updateRates() {
        try {
            Map<String, Double> rates = generateRates();

            UpdateRatesRequest request = new UpdateRatesRequest(rates);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

             headers.setBearerAuth(getAccessToken());

            HttpEntity<UpdateRatesRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ApiResponse<UpdateRatesResponse>> response = restTemplate.exchange(
                    exchangeServiceUrl + "/rates",
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<UpdateRatesResponse>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Exchange rates have been successfully updated");
            } else {
                log.warn("Failed to update courses: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error updating exchange rates", e);
        }
    }

    private String getAccessToken() {
        String tokenUrl = clientProperties.getTokenUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientProperties.getClientId(), clientProperties.getClientSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(tokenUrl, request, OAuthTokenResponse.class)
                .getAccessToken();
    }
}
