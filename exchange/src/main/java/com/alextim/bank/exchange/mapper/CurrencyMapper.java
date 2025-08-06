package com.alextim.bank.exchange.mapper;

import com.alextim.bank.common.dto.exchange.CurrencyRequest;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import com.alextim.bank.exchange.entity.Currency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class CurrencyMapper {

    public abstract Currency toEntity(CurrencyRequest currencyRequest);
    public abstract CurrencyResponse toDto(Currency currency);
}
