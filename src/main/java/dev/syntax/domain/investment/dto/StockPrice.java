package dev.syntax.domain.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockPriceDto {

    private String productCode;   // 종목코드
    private String productName;   // 종목명
    private long currentPrice;    // 현재가
    private long diffAmount;      // 전일 대비 금액
    private double diffRate;      // 전일 대비율
    private long volume;          // 누적 거래량
}
