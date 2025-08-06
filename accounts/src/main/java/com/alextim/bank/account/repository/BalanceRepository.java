package com.alextim.bank.account.repository;

import com.alextim.bank.account.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Balance SET frozenAmount = 0, locked = false WHERE locked = true AND lockingTime < :timeLimit")
    int unlockAllBefore(@Param("timeLimit") LocalDateTime timeLimit);
}
