package com.alextim.bank.account.mapper;

import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.common.dto.account.AccountBalanceResponse;
import com.alextim.bank.common.dto.account.AccountFullResponse;
import com.alextim.bank.common.dto.account.AccountRequest;
import com.alextim.bank.common.dto.account.AccountResponse;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AccountMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(accountRequest.getPassword()))")
    public abstract Account toEntity(AccountRequest accountRequest);

    @Mapping(target = "name", expression = "java(account.getFirstName() + \" \" + account.getLastName())")
    public abstract AccountResponse toDto(Account account);

    @Mapping(target = "name", expression = "java(account.getFirstName() + \" \" + account.getLastName())")
    @Mapping(target = "balances", source = "balances")
    public abstract AccountFullResponse toFullDto(Account account, List<AccountBalanceResponse> balances);

    public List<AccountBalanceResponse> toAccountBalanceResponses(List<CurrencyResponse> currencies, List<Balance> balances) {
        Map<String, CurrencyResponse> currencyResponseByCode = currencies.stream().collect(Collectors.toMap(
                CurrencyResponse::getCode, currencyResponse -> currencyResponse));

        return balances.stream().map(balance -> {
            CurrencyResponse currencyResponse = currencyResponseByCode.get(balance.getCurrencyCode());
            if(currencyResponse == null) {
                throw new RuntimeException("Unknown currency code" +  balance.getCurrencyCode());
            }
            return new AccountBalanceResponse(currencyResponse.getCode(), currencyResponse.getTitle(), balance.isOpened(), balance.getAmount());
        }).toList();
    }
}
