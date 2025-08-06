package com.alextim.bank.exchange.service;

import com.alextim.bank.exchange.entity.Currency;

import java.util.List;

public interface CurrencyService {

    Currency create(Currency currency);

    List<Currency> getAllCurrencies();
}
