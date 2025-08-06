package com.alextim.bank.exchangegenerator.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.bank")
@Getter
@Setter
@Component
public class OAuth2ClientProperties {
    private String clientId;
    private String clientSecret;
    private String tokenUrl;;
}