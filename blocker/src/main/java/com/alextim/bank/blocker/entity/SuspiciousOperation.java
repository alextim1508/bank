package com.alextim.bank.blocker.entity;

import com.alextim.bank.common.constant.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "suspicious_operations", schema = "bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SuspiciousOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String login;

    private BigDecimal amount;

    private OperationType operationType;

    private LocalDateTime timestamp;

    private String reasons;
}
