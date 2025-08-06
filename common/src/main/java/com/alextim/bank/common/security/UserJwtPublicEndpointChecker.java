package com.alextim.bank.common.security;

import java.util.List;

public interface UserJwtPublicEndpointChecker {
    boolean isPublicRequest(String uri);

    List<String> getPublicEndpoints();
}
