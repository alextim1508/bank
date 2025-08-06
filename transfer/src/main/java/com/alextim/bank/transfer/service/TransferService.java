package com.alextim.bank.transfer.service;

import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;

public interface TransferService {
    void internalTransfer(InternalTransferRequest request);

    void externalTransfer(ExternalTransferRequest request);
}
