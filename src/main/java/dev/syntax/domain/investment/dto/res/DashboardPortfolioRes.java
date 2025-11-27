package dev.syntax.domain.investment.dto.res;

import java.util.List;

public record DashboardPortfolioRes(
        Long userId,
        String depositAmount,
        String totEvluAmt,
        String totalProfitAmount,
        String totalProfitRate,
        List<HoldingItemRes> top3Holdings
) {}
