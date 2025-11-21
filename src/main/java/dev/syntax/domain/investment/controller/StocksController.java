package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.StockPrice;
import dev.syntax.domain.investment.service.StockMarketService;
import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class StocksController {
    private final KisStockApiClient kisStockApiClient;
    private final StockMarketService stockMarketService;

    public StocksController(KisStockApiClient kisStockApiClient, StockMarketService stockMarketService) {
        this.kisStockApiClient = kisStockApiClient;
        this.stockMarketService = stockMarketService;
    }



    @GetMapping("/api/test/stocks")
    public MultiPriceResponse getStockList(@RequestParam String codes) {

        // "005930,000660,035420" → List<String> 변환
        List<String> codeList = Arrays.asList(codes.split(","));

        return kisStockApiClient.getMultiPrice(codeList);
    }

    @GetMapping("/investments/stocks")
    public List<StockPrice> getStocks(
            @RequestParam String codes
    ) {
        return stockMarketService.getStockPrices(List.of(codes.split(",")));
    }

}
