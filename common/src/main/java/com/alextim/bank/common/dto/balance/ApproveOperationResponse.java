package com.alextim.bank.common.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class ApproveOperationResponse {
    private List<String> logins;
}
