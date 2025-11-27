package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.DashboardPortfolioRes;
import dev.syntax.domain.investment.service.InvestmentPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/core/investments/dashboard")
public class DashboardController {
    private final InvestmentPortfolioService portfolioService;

    /** 대시보드 조회 */
    @GetMapping("/{cano}") // cano 나중에 빼기
    public DashboardPortfolioRes getDashboardPortfolio(
            @PathVariable String cano,
            @CurrentUserId Long userId
    ) {
        return portfolioService.getDashboardPortfolio(cano, userId);
    }
}
