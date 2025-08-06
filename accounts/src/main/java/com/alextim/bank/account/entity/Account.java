package com.alextim.bank.account.entity;

import com.alextim.bank.account.constant.ContactType;
import com.alextim.bank.account.converter.RoleListToJsonConverter;
import com.alextim.bank.account.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", schema = "bank", uniqueConstraints = {
        @UniqueConstraint(columnNames = "login")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString(exclude = {"password", "balances", "contacts"})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private String login;

    @NonNull
    @Column(nullable = false)
    private String password;

    @NonNull
    @Column(nullable = false)
    private String firstName;

    @NonNull
    @Column(nullable = false)
    private String lastName;

    @NonNull
    @Column(nullable = false)
    private LocalDate birthDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean blocked = false;

    @Builder.Default
    @Convert(converter = RoleListToJsonConverter.class)
    private List<Role> roles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Balance> balances = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    public void addBalance(Balance balance) {
        balance.setAccount(this);
        balances.add(balance);
    }

    public void addContact(ContactType type, String value) {
        Contact contact = new Contact(type, value, this);
        contacts.add(contact);
    }
}
