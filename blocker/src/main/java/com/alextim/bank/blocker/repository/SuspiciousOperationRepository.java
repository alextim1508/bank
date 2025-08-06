package com.alextim.bank.blocker.repository;

import com.alextim.bank.blocker.entity.SuspiciousOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousOperationRepository extends JpaRepository<SuspiciousOperation, Long> {
}
