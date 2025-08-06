package com.alextim.bank.common.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ExternalTransferResponse {
    private String fromLogin;
    private String toLogin;
}
