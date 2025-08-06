package com.alextim.bank.exchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "currencies", schema = "bank")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Currency {

    @Id
    private String code;

    @Column(nullable = false)
    private String rusTitle;

    @Column
    private String title;

    @Column
    private String country;

    @Column
    private String mark;
}