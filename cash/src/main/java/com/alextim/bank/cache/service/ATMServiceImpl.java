package com.alextim.bank.cache.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ATMServiceImpl implements ATMService {

    @Override
    public void depositCash(BigDecimal amount) {
        log.info("The amount {} has been deposited", amount);
    }

    @Override
    public void withdrawCash(BigDecimal amount) {
        log.info("The amount {} has been withdrawn", amount);
    }
}
