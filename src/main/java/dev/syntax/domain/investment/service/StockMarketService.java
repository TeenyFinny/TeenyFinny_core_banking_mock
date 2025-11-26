package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.StockPrice;
import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceRes;
import dev.syntax.external.kis.dto.PriceItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMarketService {

    private final KisStockApiClient kisClient;

    public Map<String, StockPrice> getPriceMap(List<String> codes) {

        MultiPriceRes response = kisClient.getPrices(codes);

        return response.getOutput().stream()
                .collect(Collectors.toMap(
                        PriceItem::getProductCode,
                        p -> new StockPrice(
                                p.getProductCode(),
                                p.getProductName(),
                                Long.parseLong(p.getCurrentPrice()),
                                Long.parseLong(p.getPrevPriceChange()),
                                Double.parseDouble(p.getPrevRate()),
                                Long.parseLong(p.getAccumulatedVolume())
                        )
                ));
    }
}
