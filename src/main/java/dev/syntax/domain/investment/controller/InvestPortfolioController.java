package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.InvestPortfolioRes;
import dev.syntax.domain.investment.dto.res.PortfolioDateRes;
import dev.syntax.domain.investment.service.MonthlyPortfolioService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/investments/portfolio")
@RequiredArgsConstructor
public class InvestPortfolioController {
    private final MonthlyPortfolioService monthlyService;

    @GetMapping("/dates")
    public List<PortfolioDateRes> getAvailableDates(
            @CurrentUserId Long userId,
            @RequestParam String cano
    ) {
        return monthlyService.getAvailableDates(cano, userId);
    }


    @GetMapping
    public InvestPortfolioRes getMonthlyPortfolio(
            @CurrentUserId Long userId,
            @RequestParam String cano,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return monthlyService.getMonthlyPortfolio(cano, userId, year, month);
    }
}