package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.TransferServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.ExternalTransferResponse;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferResponse;
import com.alextim.bank.common.exception.ConvertClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class TransferClientUnits {

    public static InternalTransferResponse makeInternalTransfer(TransferServiceClient client,
                                                                InternalTransferRequest request) {

        log.info("Send 'makeInternalTransfer' request to exchange service");
        var response = client.makeInternalTransfer(request);
        log.info("Response of 'makeInternalTransfer': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new ConvertClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static ExternalTransferResponse makeExternalTransfer(TransferServiceClient client,
                                                                ExternalTransferRequest request) {

        log.info("Send 'makeExternalTransfer' request to exchange service");
        var response = client.makeExternalTransfer(request);
        log.info("Response of 'makeExternalTransfer': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new ConvertClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
