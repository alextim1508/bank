package com.alextim.bank.common.config;

import com.alextim.bank.common.client.OAuth2TokenClient;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class FeignClientConfig {

    @Value("${sso.client-registration-id}")
    private String clientRegistrationId;

    @Value("${spring.application.name}")
    private String principal;

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2TokenClient tokenClient) {
        return template -> {
            String token = tokenClient.getBearerToken(clientRegistrationId, principal);
            template.header(HttpHeaders.AUTHORIZATION, token);
        };
    }
}
