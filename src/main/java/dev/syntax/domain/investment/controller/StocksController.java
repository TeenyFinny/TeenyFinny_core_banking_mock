package dev.syntax.domain.investment.controller;

import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceRes;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StocksController {
    private final KisStockApiClient kisStockApiClient;

    @GetMapping("/investments/stocks")
    public ResponseEntity<BaseResponse<?>> getStocks() {
        MultiPriceRes res = kisStockApiClient.getMultiPrice();
        return ApiResponseUtil.success(SuccessCode.OK, res);
    }

}