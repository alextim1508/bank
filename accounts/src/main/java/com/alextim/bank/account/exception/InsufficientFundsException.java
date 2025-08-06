package com.alextim.bank.account.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InsufficientFundsException extends RuntimeException {

    private final String login;
    private final String currency;
    private final BigDecimal accountAmount;
    private final BigDecimal debitAmount;

    public InsufficientFundsException(String login, String currency, BigDecimal accountAmount, BigDecimal debitAmount) {
        super(String.format(
                "Insufficient funds on the balance with login %s (current balance: %s %s) to debit %s",
                login,
                accountAmount.stripTrailingZeros().toPlainString(),
                currency,
                debitAmount.stripTrailingZeros().toPlainString()));
        this.login = login;
        this.currency = currency;
        this.accountAmount = accountAmount;
        this.debitAmount = debitAmount;
    }
}



