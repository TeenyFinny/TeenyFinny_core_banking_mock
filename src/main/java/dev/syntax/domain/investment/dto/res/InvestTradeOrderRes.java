package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.global.service.Utils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvestTradeOrderRes {
    private TradeType tradeType;

    private String productCode;

    private String productName;

    private long quantity;

    private String price;

    private OrderStatus status;

    public static InvestTradeOrderRes from(TradeOrder order) {
        return InvestTradeOrderRes.builder()
                .tradeType(order.getTradeType())
                .productCode(order.getProductCode())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(Utils.NumberFormattingService(order.getPrice()))
                .status(order.getStatus())
                .build();
    }
}
