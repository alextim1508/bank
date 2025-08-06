package com.alextim.bank.common.dto.auth;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenStatusResponse {
    private boolean valid;
    private boolean blacklisted;
    private boolean expired;
}
