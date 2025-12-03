package dev.syntax.domain.investment.dto.cal;

import dev.syntax.domain.investment.dto.HoldingItem;
import lombok.Builder;

import java.util.List;

@Builder
public record InvestPortfolioCalcResult(
        String cano,
        Long userId,
        Long depositAmount,
        Long totalAssetAmount,
        Long totalEvaluationAmount,
        Long totalProfitAmount,
        Double totalProfitRate,
        List<HoldingItem> holdings  // 비중 weight 포함된 최종 리스트
) {}

