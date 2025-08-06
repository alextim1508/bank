package com.alextim.bank.common.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenClient {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private final ClientRegistrationRepository clientRegistrationRepository;

    public String getBearerToken(String clientRegistrationId, String principal) {
        try {
            ClientRegistration clientRegistration = clientRegistrationRepository
                    .findByRegistrationId(clientRegistrationId);

            if (clientRegistration == null) {
                log.error("Client registration '{}' not found", clientRegistrationId);
                throw new IllegalStateException("OAuth2 client registration not found");
            }

            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistrationId)
                    .principal(principal)
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient == null) {
                log.error("Failed to obtain authorized client for '{}'", clientRegistrationId);
                throw new IllegalStateException("Authorized client is null");
            }

            return "Bearer " + authorizedClient.getAccessToken().getTokenValue();

        } catch (Exception e) {
            log.error("Failed to obtain access token for client '{}'", clientRegistrationId, e);
            throw new RuntimeException("Failed to get bearer token", e);
        }
    }
}