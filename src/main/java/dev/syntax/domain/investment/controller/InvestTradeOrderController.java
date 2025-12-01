package dev.syntax.domain.investment.controller;


import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.service.InvestTradeOrderService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/investments/trade")
@RequiredArgsConstructor
public class InvestTradeOrderController {

    private final InvestTradeOrderService investTradeOrderService;

    /**
     * 매수 주문
     */
    @PostMapping("/buy")
    public InvestTradeOrderRes buy(@RequestBody BuyReq req, @CurrentUserId Long userId) {

        TradeOrder order = investTradeOrderService.buy(
                req.getCano(),
                userId,
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                Long.parseLong(req.getPrice().replaceAll(",", ""))
        );

        return InvestTradeOrderRes.from(order);
    }

    /**
     * 매도 주문
     */
    @PostMapping("/sell")
    public InvestTradeOrderRes sell(@RequestBody SellReq req, @CurrentUserId Long userId) {

        TradeOrder order = investTradeOrderService.sell(
                req.getCano(),
                userId,
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                Long.parseLong(req.getPrice().replaceAll(",", ""))
        );

        return InvestTradeOrderRes.from(order);
    }
}
