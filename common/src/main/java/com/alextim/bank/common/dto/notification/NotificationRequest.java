package com.alextim.bank.common.dto.notification;

import com.alextim.bank.common.constant.AggregateType;
import com.alextim.bank.common.constant.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class NotificationRequest {

    @NotNull
    private AggregateType aggregateType;

    @NotNull
    private EventType eventType;

    @NotBlank
    @Size
    private String login;

    @NotBlank
    @Size
    private String message;
}
