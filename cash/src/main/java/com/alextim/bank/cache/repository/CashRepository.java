package com.alextim.bank.cache.repository;

import com.alextim.bank.cache.entity.CashOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashRepository extends JpaRepository<CashOperation, Long> {
}
