package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestDashboardPortfolioRes;
import dev.syntax.domain.investment.service.InvestPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/core/investments/dashboard")
@Slf4j
public class InvestDashboardController {
    private final InvestPortfolioService portfolioService;

    /** 대시보드 조회 */
    @GetMapping("/{cano}") // cano 나중에 빼기
    public InvestDashboardPortfolioRes getDashboardPortfolio(
            @PathVariable String cano,
            @CurrentUserId Long userId
    ) {
        return portfolioService.getDashboardPortfolio(cano, userId);
    }
}
