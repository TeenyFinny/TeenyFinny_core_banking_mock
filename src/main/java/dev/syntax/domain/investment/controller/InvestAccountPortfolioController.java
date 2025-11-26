package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.AccountPortfolioRes;
import dev.syntax.domain.investment.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments/account")
public class InvestAccountPortfolioController {

    private static final String CORE_USER_ID_HEADER = "X-Core-User-Id";
    private final PortfolioService portfolioService;

    /** 2. 내 계좌 전체 정보 */
    @GetMapping("/{cano}/{userId}") // cano 나중에 빼기
    public AccountPortfolioRes getAccountPortfolio(
            @PathVariable String cano,
            @PathVariable Long userId
//            @RequestHeader(CORE_USER_ID_HEADER) Long userId // Header에서 추출
    ) {
        return portfolioService.getAccountPortfolio(cano, userId);
    }
}
