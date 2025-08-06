package com.alextim.bank.account.entity;

import com.alextim.bank.account.constant.ContactType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts", schema = "bank", uniqueConstraints = {
        @UniqueConstraint(columnNames = "value")
})
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "account")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type;

    @NonNull
    @Column(nullable = false, unique = true)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NonNull
    private Account account;
}
