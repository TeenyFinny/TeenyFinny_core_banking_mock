package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.domain.investment.service.StocksService;
import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceRes;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core/investments/stocks")
@Slf4j
public class StocksController {
    private final StocksService stocksService;

    @GetMapping("/buy")
    public MultiPriceRes getStocksForBuy(){
        return stocksService.getStocksForBuy();
    }

    @GetMapping("/sell")
    public MultiPriceRes getStocksForSell(
            @CurrentUserId Long userId) {
        return stocksService.getStocksForSell(userId);
    }

    @GetMapping("/detail/{code}")
    public StockDetailRes getStockDetail(
            @PathVariable String code,
            @CurrentUserId Long userId
    ) {
        StockDetailRes stockDetail = stocksService.getStockDetail(userId, code);
        return stockDetail;
    }
}