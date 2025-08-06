package com.alextim.bank.cache.entity;

import com.alextim.bank.cache.constant.CashOperationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations", schema = "bank")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NonNull
    private String login;

    @Column(nullable = false)
    @NonNull
    private String currency;

    @Column(nullable = false)
    @NonNull
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NonNull
    private CashOperationType operationType;
}
