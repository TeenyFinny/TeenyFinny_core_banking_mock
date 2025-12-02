package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.external.kis.dto.MultiPriceRes;

public interface StocksService {
    MultiPriceRes getStocksForBuy();
    MultiPriceRes getStocksForSell(Long userId);
    StockDetailRes getStockDetail(Long userId, String code);
}
