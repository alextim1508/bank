package com.alextim.bank.cache.service;

import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.WithdrawRequest;

public interface CashService {

    void deposit(DepositRequest request);

    void withdraw(WithdrawRequest request);
}
