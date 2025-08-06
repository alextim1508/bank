package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.exchange.*;
import com.alextim.bank.common.exception.ConvertClientException;
import com.alextim.bank.common.exception.CurrencyFetchException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ExchangeClientUtils {

    public static UpdateRatesResponse updateRates(ExchangeServiceClient client, UpdateRatesRequest request) {

        log.info("Send 'updateRates' request to exchange service");
        var response = client.updateRates(request);
        log.info("Response of 'updateRates': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new CurrencyFetchException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static List<RateResponseDto> getRates(ExchangeServiceClient client) {

        log.info("Send 'getRates' request to exchange service");
        var response = client.getRates();
        log.info("Response of 'getRates': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new CurrencyFetchException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static List<CurrencyResponse> getCurrencies(ExchangeServiceClient client) {

        log.info("Send 'fetchCurrencies' request to exchange service");
        var response = client.getAllCurrencies();
        log.info("Response of 'fetchCurrencies': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new CurrencyFetchException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static ConversionResponse convertAmount(ExchangeServiceClient client,
                                                   ConversionRequest request) {

        log.info("Send 'convertAmount' request to exchange service");
        var response = client.convert(request);
        log.info("Response of 'convertAmount': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new ConvertClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
