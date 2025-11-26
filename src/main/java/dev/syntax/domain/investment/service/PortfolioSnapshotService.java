package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.HoldingItem;
import dev.syntax.domain.investment.dto.cal.PortfolioCalcResult;
import dev.syntax.domain.investment.entity.PortfolioMonthly;
import dev.syntax.domain.investment.entity.PortfolioMonthlySummary;
import dev.syntax.domain.investment.repository.PortfolioMonthlyRepository;
import dev.syntax.domain.investment.repository.PortfolioMonthlySummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotService {

    private final PortfolioService portfolioService;
    private final PortfolioMonthlyRepository monthlyRepo;
    private final PortfolioMonthlySummaryRepository summaryRepo;

    @Transactional
    public void createMonthlySnapshot(String cano, Long userId, int year, int month) {

        PortfolioCalcResult calc = portfolioService.calculatePortfolio(cano, userId);

        if (calc.holdings() == null || calc.holdings().isEmpty()) {
             log.info("보유한 주식이 없어 포트폴리오를 생성할 수 없습니다. cano: {}", cano);
            return;
        }

        // ---- 1) Top 보유종목 계산 ----
        List<HoldingItem> sorted = calc.holdings().stream()
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .toList();

        String top1Name = sorted.size() >= 1 ? sorted.get(0).productName() : null;
        Double top1Weight = sorted.size() >= 1 ? sorted.get(0).weight() : null;

        String top2Name = sorted.size() >= 2 ? sorted.get(1).productName() : null;
        Double top2Weight = sorted.size() >= 2 ? sorted.get(1).weight() : null;

        String top3Name = sorted.size() >= 3 ? sorted.get(2).productName() : null;
        Double top3Weight = sorted.size() >= 3 ? sorted.get(2).weight() : null;

        Double etcWeight = sorted.size() > 3
                ? sorted.stream().skip(3).mapToDouble(HoldingItem::weight).sum()
                : null;

        // ---- 2) Summary 저장 ----
        PortfolioMonthlySummary summary = PortfolioMonthlySummary.builder()
                .cano(cano)
                .userId(userId)
                .year(year)
                .month(month)
                .depositAmount(calc.depositAmount())
                .totalEvaluationAmount(calc.totalEvaluationAmount())
                .totalProfitAmount(calc.totalProfitAmount())
                .totalProfitRate(calc.totalProfitRate())
                .top1Name(top1Name)
                .top1Weight(top1Weight)
                .top2Name(top2Name)
                .top2Weight(top2Weight)
                .top3Name(top3Name)
                .top3Weight(top3Weight)
                .etcWeight(etcWeight)
                .createdAt(LocalDateTime.now())
                .build();

        summaryRepo.save(summary);

        // ---- 3) 종목별 스냅샷 저장 ----
        List<PortfolioMonthly> snapshots = calc.holdings().stream()
                .map(h -> PortfolioMonthly.builder()
                        .cano(cano)
                        .userId(userId)
                        .year(year)
                        .month(month)
                        .productCode(h.productCode())
                        .productName(h.productName())
                        .holdingQuantity(h.quantity())
                        .purchaseAvgPrice(h.avgPrice())
                        .currentPrice(h.currentPrice())
                        .evaluationAmount(h.evaluationAmount())
                        .profitAmount(h.profitAmount())
                        .profitRate(h.profitRate())
                        .weight(h.weight())
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();
        monthlyRepo.saveAll(snapshots);
    }
}
