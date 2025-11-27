package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.global.service.Utils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeOrderRes {
    private TradeType tradeType;

    private String productCode;

    private String productName;

    private long quantity;

    private String price;

    private OrderStatus status;

    public static TradeOrderRes from(TradeOrder order) {
        return TradeOrderRes.builder()
                .tradeType(order.getTradeType())
                .productCode(order.getProductCode())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(Utils.NumberFormattingService(order.getPrice()))
                .status(order.getStatus())
                .build();
    }
}
