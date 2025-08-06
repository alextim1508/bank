package com.alextim.bank.account.service;

import com.alextim.bank.common.dto.balance.ApproveOperationRequest;
import com.alextim.bank.common.dto.balance.CreditBalanceRequest;
import com.alextim.bank.common.dto.balance.DebitBalanceRequest;

public interface BalanceService {

    void creditBalance(CreditBalanceRequest request);

    void debitBalance(DebitBalanceRequest request);

    void approve(ApproveOperationRequest request);
}
