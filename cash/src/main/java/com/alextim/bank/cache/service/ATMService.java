package com.alextim.bank.cache.service;

import java.math.BigDecimal;

public interface ATMService {

    void depositCash(BigDecimal amount);

    void withdrawCash(BigDecimal amount);
}
