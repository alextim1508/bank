package com.alextim.bank.account.mapper;

import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    CurrencyResponse toDto(Balance balance);
}