package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.AccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.DashboardPortfolioRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/portfolio")
public class PortfolioController {

    private static final String CORE_USER_ID_HEADER = "X-Core-User-Id";
    private final PortfolioService portfolioService;

    /** 1. 포트폴리오 상세 조회 */
    @GetMapping("/{cano}/{userId}") // cno 나중에 빼기
    public PortfolioRes getPortfolio(
            @PathVariable String cano,
            @PathVariable Long userId // 헤더에서 추출로 변경
//            @RequestHeader(CORE_USER_ID_HEADER) Long userId // Header에서 추출
    ) {
        return portfolioService.getPortfolio(cano, userId);
    }


}
