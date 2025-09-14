package com.alextim.bank.common.config;

import com.alextim.bank.common.client.OAuth2TokenClient;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Configuration
public class FeignClientConfig {

    @Value("${sso.client-registration-id}")
    private String clientRegistrationId;

    @Value("${spring.application.name}")
    private String principal;

    @Bean
    public RequestInterceptor requestInterceptor(OAuth2TokenClient tokenClient) {
        return template -> {
            addAuthorizationHeader(tokenClient, template);

            copyXHeaders(template);
        };
    }

    private void addAuthorizationHeader(OAuth2TokenClient tokenClient, RequestTemplate template) {
        String token = tokenClient.getBearerToken(clientRegistrationId, principal);
        template.header(HttpHeaders.AUTHORIZATION, token);
    }

    private void copyXHeaders(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();

            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();

                if (headerName.toLowerCase().startsWith("x-")) {
                    String headerValue = request.getHeader(headerName);
                    template.header(headerName, headerValue);
                }
            }
        }
    }
}
