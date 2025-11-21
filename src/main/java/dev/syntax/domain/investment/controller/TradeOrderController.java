package dev.syntax.domain.investment.controller;


import dev.syntax.domain.investment.dto.req.BuyReq;
import dev.syntax.domain.investment.dto.req.SellReq;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.service.TradeOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invest/trade")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    /**
     * 매수 주문
     */
    @PostMapping("/buy")
    public TradeOrder buy(@RequestBody BuyReq req) {

        return tradeOrderService.buy(
                req.getCano(),
                req.getUserId(),
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                req.getPrice()
        );
    }

    /**
     * 매도 주문
     */
    @PostMapping("/sell")
    public TradeOrder sell(@RequestBody SellReq req) {

        return tradeOrderService.sell(
                req.getCano(),
                req.getUserId(),
                req.getProductCode(),
                req.getProductName(),
                req.getQuantity(),
                req.getPrice()
        );
    }
}
