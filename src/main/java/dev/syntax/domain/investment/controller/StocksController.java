package dev.syntax.domain.investment.controller;

import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core/investments")
public class StocksController {
    private final KisStockApiClient kisStockApiClient;

    @GetMapping("/stocks")
    public MultiPriceRes getStocks() {
        return kisStockApiClient.getMultiPrice();
    }

}