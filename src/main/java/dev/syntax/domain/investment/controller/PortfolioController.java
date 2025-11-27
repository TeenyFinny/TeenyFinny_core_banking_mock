package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.InvestmentPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/portfolio")
public class PortfolioController {

    private static final String CORE_USER_ID_HEADER = "X-Core-User-Id";
    private final InvestmentPortfolioService portfolioService;

    /** 1. 포트폴리오 상세 조회 */
    @GetMapping("/{cano}") // cno 나중에 빼기
    public PortfolioRes getPortfolio(
            @PathVariable String cano,
            @CurrentUserId Long userId // 헤더에서 추출로 변경
    ) {
        return portfolioService.getPortfolio(cano, userId);
    }


}
