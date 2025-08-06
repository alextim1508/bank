package com.alextim.bank.common.dto.blocker;

import com.alextim.bank.common.constant.OperationType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class OperationCheckRequest {

    @NotBlank(message = "{operation.login.notblank}")
    private String login;

    @NotNull(message = "{operation.amount.required}")
    @DecimalMin(value = "0.01", message = "{operation.amount.positive}")
    private BigDecimal amount;

    @NotNull(message = "{operation.type.required}")
    private OperationType operationType;

    @NotNull(message = "{operation.timestamp.required}")
    @PastOrPresent (message = "{operation.timestamp.past}")
    private LocalDateTime timestamp;
}
