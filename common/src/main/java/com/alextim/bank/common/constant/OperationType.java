package com.alextim.bank.common.constant;

public enum OperationType {
    DEBIT,      // списание средств со счёта
    CREDIT,     //зачисление средств на счёт

    CASH_WITHDRAW,   //снятие средств со своего счёта
    CASH_DEPOSIT,    //пополнение своего счёта
}
