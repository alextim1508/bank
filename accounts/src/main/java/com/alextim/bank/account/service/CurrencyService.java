package com.alextim.bank.account.service;

import com.alextim.bank.common.dto.exchange.CurrencyResponse;

import java.util.List;

public interface CurrencyService {

    List<CurrencyResponse> getCurrencies();
}
