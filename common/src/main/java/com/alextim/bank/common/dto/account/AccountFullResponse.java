package com.alextim.bank.common.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class AccountFullResponse {
    private String login;
    private String name;
    private LocalDate birthDate;

    @JsonProperty("balances")
    private List<AccountBalanceResponse> balances;
}
