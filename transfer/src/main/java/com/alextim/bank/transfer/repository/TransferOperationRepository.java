package com.alextim.bank.transfer.repository;

import com.alextim.bank.transfer.entity.TransferOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferOperationRepository extends JpaRepository<TransferOperation, Long> {
}
