package com.alextim.bank.blocker.service;

import com.alextim.bank.common.dto.blocker.OperationCheckRequest;

public interface BlockerService {
    boolean isSuspicious(OperationCheckRequest request);
}
