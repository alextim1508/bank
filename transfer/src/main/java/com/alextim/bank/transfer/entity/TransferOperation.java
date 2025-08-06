package com.alextim.bank.transfer.entity;

import com.alextim.bank.transfer.constant.TransferOperationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transfer_operations", schema = "bank")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NonNull
    private String fromLogin;

    @Column(nullable = false)
    @NonNull
    private String fromCurrency;

    @Column(nullable = false)
    @NonNull
    private String toLogin;

    @Column(nullable = false)
    @NonNull
    private String toCurrency;

    @Column(nullable = false)
    @NonNull
    private BigDecimal amount;

    @Column(nullable = false)
    @NonNull
    private BigDecimal convertedAmount;

    @Column
    private Double fromExchangeRateToRub;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NonNull
    private TransferOperationType operationType;
}
