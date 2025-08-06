package com.alextim.bank.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperty {

    private String secret;

    private int accessExpiration;

    private int refreshExpiration;

    private String loginHeaderName;

    private String accessTokenName;

    private String refreshTokenName;
}
