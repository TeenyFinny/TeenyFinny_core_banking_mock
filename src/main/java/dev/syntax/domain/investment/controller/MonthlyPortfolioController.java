package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.service.MonthlyPortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/investments/portfolio")
@RequiredArgsConstructor
public class MonthlyPortfolioController {
    private static final String CORE_USER_ID_HEADER = "X-Core-User-Id";
    private final MonthlyPortfolioService monthlyService;

    @GetMapping(path = "/{cano}/{userId}", params = {"year", "month"})
    public PortfolioRes getMonthly(
            @PathVariable String cano,
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
//            @RequestHeader(CORE_USER_ID_HEADER) Long userId // Header에서 추출
    ) {
        return monthlyService.getMonthlyPortfolio(cano, userId, year, month);
    }
}