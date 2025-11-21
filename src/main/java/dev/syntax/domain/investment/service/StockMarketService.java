package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.StockPrice;
import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceResponse;
import dev.syntax.external.kis.dto.PriceItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMarketService {

    private final KisStockApiClient kisStockApiClient;

    /**
     * 한국투자증권 주식 시세 조회 → 내부 표준 DTO 변환
     */
    public List<StockPrice> getStockPrices(List<String> codes) {

        MultiPriceResponse response = kisStockApiClient.getMultiPrice(codes);

        if (response == null || response.getOutput() == null) {
            throw new IllegalStateException("KIS API response is null or invalid.");
        }

        return response.getOutput().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * PriceItem → StockPriceDto 변환
     */
    private StockPrice convertToDto(PriceItem item) {
        return new StockPrice(
                item.getStockCode(),
                item.getStockName(),
                parseLong(item.getCurrentPrice()),
                parseLong(item.getPreviousDayDiff()),
                parseDouble(item.getPreviousDayRate()),
                parseLong(item.getAccumulatedVolume())
        );
    }

    /**
     * 숫자(Long) 변환 (빈 값 또는 null 대비)
     */
    private long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) return 0;
            return Long.parseLong(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 숫자(Double) 변환
     */
    private double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) return 0.0;
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
