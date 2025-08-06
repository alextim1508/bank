package com.alextim.bank.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balances", schema = "bank")
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"account"})
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column
    private String currencyCode;

    @NonNull
    @Builder.Default
    @Column
    private BigDecimal amount = BigDecimal.ZERO;

    @NonNull
    @Builder.Default
    @Column
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column
    private boolean locked = false;

    @Column
    private LocalDateTime lockingTime;

    @Builder.Default
    @Column
    private boolean opened = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NonNull
    private Account account;

}
