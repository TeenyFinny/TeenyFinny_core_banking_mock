package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.entity.TradeOrder;

public interface TradeOrderService {
    public TradeOrder buy(
            String cano,          // CHAR(8)
            Long userId,          // BIGINT
            String productCode,   // pdno
            String productName,   // prdt_name
            int quantity,         // ord_qty (INT)
            long price            // ord_unpr (BIGINT)
    );
    public TradeOrder sell(
            String cano,
            Long userId,
            String productCode,
            String productName,
            int quantity,
            long price
    );
}
