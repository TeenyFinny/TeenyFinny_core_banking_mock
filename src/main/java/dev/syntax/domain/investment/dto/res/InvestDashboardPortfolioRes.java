package dev.syntax.domain.investment.dto.res;

import java.util.List;

public record InvestDashboardPortfolioRes(
        Long userId,
        String depositAmount,
        String totalAssetAmount,
        String totEvluAmt,
        String totalProfitAmount,
        String totalProfitRate,
        List<HoldingItemRes> top3Holdings
) {}
