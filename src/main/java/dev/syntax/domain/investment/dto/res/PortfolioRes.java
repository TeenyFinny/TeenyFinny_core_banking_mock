package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.dto.HoldingItem;
import dev.syntax.domain.investment.dto.TopHoldingItem;

import java.util.List;

public record PortfolioRes(
        Long userId,
        Long depositAmount,                 // 예수금
        Long totEvluAmt,         // 총 평가금액 (실시간)
        Long totalProfitAmount,             // 총 수익금
        Double totalProfitRate,             // 총 수익률
        List<HoldingItem> holdings,         // 보유 종목 상세 리스트
        List<TopHoldingItem> topHoldings    // 상위 3개 + 기타
) {}

