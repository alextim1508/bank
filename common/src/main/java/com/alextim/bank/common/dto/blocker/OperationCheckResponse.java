package com.alextim.bank.common.dto.blocker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class OperationCheckResponse {
    private String login;
    private boolean approved;
}
