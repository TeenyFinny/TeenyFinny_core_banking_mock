package dev.syntax.domain.investment.controller;


import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.TradeOrderRes;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.service.TradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/investments/trade")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    /**
     * 매수 주문
     */
    @PostMapping("/buy")
    public TradeOrderRes buy(@RequestBody BuyReq req, @CurrentUserId Long userId) {

        TradeOrder order = tradeOrderService.buy(
                req.getCano(),
                userId,
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                req.getPrice()
        );

        return TradeOrderRes.from(order);
    }

    /**
     * 매도 주문
     */
    @PostMapping("/sell")
    public TradeOrderRes sell(@RequestBody SellReq req, @CurrentUserId Long userId) {

        TradeOrder order = tradeOrderService.sell(
                req.getCano(),
                userId,
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                req.getPrice()
        );

        return TradeOrderRes.from(order);
    }
}
