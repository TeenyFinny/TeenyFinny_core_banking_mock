package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.service.InvestPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core/investments/account")
public class InvestAccountPortfolioController {

    private final InvestPortfolioService portfolioService;

    /** 내 계좌 전체 정보 */
    @GetMapping("/{cano}")
    public InvestAccountPortfolioRes getAccountPortfolio(
            @PathVariable String cano,
            @CurrentUserId Long userId
    ) {
        return portfolioService.getAccountPortfolio(cano, userId);
    }
}
