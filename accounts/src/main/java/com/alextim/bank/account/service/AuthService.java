package com.alextim.bank.account.service;


import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;

public interface AuthService {

    TokenPairResponse generateToken(LoginRequest request);

    TokenPairResponse refreshToken(RefreshRequest request);
}
