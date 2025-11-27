package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.dto.TopHoldingItem;

import java.util.List;

public record InvestPortfolioRes(
        Long userId,
        String  depositAmount,                 // 예수금
        String  totEvluAmt,         // 총 평가금액 (실시간)
        String  totalProfitAmount,             // 총 수익금
        String totalProfitRate,             // 총 수익률
        List<HoldingItemRes> holdings,         // 보유 종목 상세 리스트
        List<TopHoldingItem> topHoldings    // 상위 3개 + 기타
) {}

