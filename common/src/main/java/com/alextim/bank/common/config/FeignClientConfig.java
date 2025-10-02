package com.alextim.bank.common.config;

import com.alextim.bank.common.client.OAuth2TokenClient;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FeignClientConfig {

    @Value("${sso.client-registration-id}")
    private String clientRegistrationId;

    @Value("${spring.application.name}")
    private String principal;

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2TokenClient tokenClient,
                                                 Tracer tracer,
                                                 Propagator propagator) {
        return template -> {
            addAuthorizationHeader(tokenClient, template);

            addTracingHeaders(template, tracer, propagator);
        };
    }

    private void addAuthorizationHeader(OAuth2TokenClient tokenClient, RequestTemplate template) {
        String token = tokenClient.getBearerToken(clientRegistrationId, principal);
        template.header(HttpHeaders.AUTHORIZATION, token);
    }

    private void addTracingHeaders(RequestTemplate template, Tracer tracer, Propagator propagator) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            Map<String, String> tracingHeaders = new HashMap<>();

            propagator.inject(currentSpan.context(), tracingHeaders, Map::put);

            tracingHeaders.forEach((key, value) -> {
                if (!template.headers().containsKey(key)) {
                    template.header(key, value);
                }
            });
        }
    }
}
