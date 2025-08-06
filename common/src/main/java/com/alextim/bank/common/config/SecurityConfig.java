package com.alextim.bank.common.config;

import com.alextim.bank.common.security.UserJwtPublicEndpointChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/front/login").permitAll()
                        .requestMatchers("/front/signup").permitAll()


                        .requestMatchers("/front/main").permitAll()
                        .requestMatchers("/front/account/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/account").permitAll()
                        .requestMatchers("/account/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/exchange/currency").permitAll()
                        .requestMatchers(HttpMethod.GET,"/exchange/rates").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers("/front/**").authenticated()
                        .requestMatchers("/account/**").authenticated()
                        .requestMatchers("/exchange/**").authenticated()
                        .requestMatchers("/blocker/**").authenticated()
                        .requestMatchers("/cash/**").authenticated()
                        .requestMatchers("/transfer/**").authenticated()
                        .requestMatchers("/notification/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(AbstractHttpConfigurer::disable) //todo 
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                        )
                );

        return http.build();
    }

    @Bean
    public UserJwtPublicEndpointChecker publicEndpointChecker() {
        return new UserJwtPublicEndpointChecker() {
            private final List<String> publicEndpoints = List.of(
                    "/account/auth/login",
                    "/account/auth/check",
                    "/exchange/rates",
                    "/actuator/health",
                    "/account",
                    "/front/login",
                    "/front/signup"
            );

            @Override
            public boolean isPublicRequest(String uri) {
                return publicEndpoints.stream().anyMatch(s -> s.equals(uri));
            }

            @Override
            public List<String> getPublicEndpoints() {
                return List.copyOf(publicEndpoints);
            }
        };
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("realm_access");

        return jwt -> {
            Collection<GrantedAuthority> authorities = Stream.concat(
                    authoritiesConverter.convert(jwt).stream(),
                    extractScopes(jwt).stream()
            ).collect(Collectors.toList());

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    private Collection<GrantedAuthority> extractScopes(Jwt jwt) {
        Object scopes = jwt.getClaims().get("scope");
        if (scopes == null) return List.of();

        String[] scopeArray = ((String) scopes).split(" ");
        return Arrays.stream(scopeArray)
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

