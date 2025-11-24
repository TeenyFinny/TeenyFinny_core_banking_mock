package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.HoldingItem;
import dev.syntax.domain.investment.dto.StockPrice;
import dev.syntax.domain.investment.dto.TopHoldingItem;
import dev.syntax.domain.investment.dto.cal.PortfolioCalcResult;
import dev.syntax.domain.investment.dto.res.AccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.DashboardPortfolioRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.entity.Portfolio;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import dev.syntax.domain.investment.repository.PortfolioRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final InvestmentAccountRepository accountRepository;
    private final StockMarketService stockMarketService;

    /**
     * 공통 포트폴리오 계산 로직
     * 모든 API는 이 계산 결과를 기반으로 응답만 다르게 만든다.
     */
    private PortfolioCalcResult calculatePortfolio(String cano, Long userId) {

        // 1) 계좌 조회
        InvestmentAccount account = accountRepository.findByCano(cano)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.ACCOUNT_NOT_FOUND));

        // 2) 포트폴리오 로드
        List<Portfolio> portfolios = portfolioRepository.findByCano_Cano(cano);

        // 보유 종목 리스트
        List<String> productCodes = portfolios.stream()
                .map(Portfolio::getProductCode)
                .toList();

        if (productCodes.isEmpty()) {
            // 보유 종목이 전혀 없는 경우
            return PortfolioCalcResult.builder()
                    .userId(userId)
                    .depositAmount(account.getDepositAmount())
                    .totalEvaluationAmount(0L)
                    .totalProfitAmount(0L)
                    .totalProfitRate(0.0)
                    .holdings(Collections.emptyList())
                    .build();
        }

        // 3) 현재가 로드
        Map<String, StockPrice> prices = stockMarketService.getPriceMap(productCodes);

        long totalEvalAmount = 0L;
        long totalPurchaseAmount = 0L; // 총 매입 금액 집계 변수
        List<HoldingItem> items = new ArrayList<>();

        // 4) 종목별 계산
        for (Portfolio p : portfolios) {
            StockPrice priceDto = prices.get(p.getProductCode());
            if (priceDto == null) continue;

            long currentPrice = priceDto.getCurrentPrice();
            long purchaseAmount = p.getPurchaseAvgPrice() * p.getHoldingQuantity();
            long evaluationAmount = currentPrice * p.getHoldingQuantity();
            long profitAmount = evaluationAmount - purchaseAmount;
            double profitRate = purchaseAmount == 0 ? 0.0 :
                    ((double) profitAmount / purchaseAmount) * 100.0;

            totalEvalAmount += evaluationAmount;
            totalPurchaseAmount += purchaseAmount;

            items.add(new HoldingItem(
                    p.getProductCode(),
                    p.getProductName(),
                    p.getHoldingQuantity(),
                    p.getPurchaseAvgPrice(),
                    currentPrice,
                    evaluationAmount,
                    profitAmount,
                    profitRate,
                    0.0 // weight will be calculated next
            ));
        }

        // 5) 총 손익 및 수익률 계산
        long totalProfitAmount = totalEvalAmount - totalPurchaseAmount;
        // 총 수익률 분모는 총 매입 금액 (0 방지)
        long totalProfitDenominator = totalPurchaseAmount == 0 ? 1 : totalPurchaseAmount;
        double totalProfitRate = ((double) totalProfitAmount / totalProfitDenominator) * 100.0;

        // 6) 비중 계산
        double totalEvalDenominator = totalEvalAmount == 0 ? 1.0 : totalEvalAmount;

        List<HoldingItem> updatedItems = items.stream()
                .map(h -> new HoldingItem(
                        h.productCode(),
                        h.productName(),
                        h.quantity(),
                        h.avgPrice(),
                        h.currentPrice(),
                        h.evaluationAmount(),
                        h.profitAmount(),
                        h.profitRate(),
                        ((double) h.evaluationAmount() / totalEvalDenominator) * 100
                ))
                .toList();

        // 최종 계산 결과 전달
        return PortfolioCalcResult.builder()
                .userId(userId)
                .depositAmount(account.getDepositAmount())
                .totalEvaluationAmount(totalEvalAmount)
                .totalProfitAmount(totalProfitAmount)
                .totalProfitRate(totalProfitRate)
                .holdings(updatedItems)
                .build();
    }

    /**
     *  1. 포트폴리오 상세 조회 API
     * 상위 3개 + 기타 포함
     */
    public PortfolioRes getPortfolio(String cano, Long userId) {

        PortfolioCalcResult calc = calculatePortfolio(cano, userId);

        List<TopHoldingItem> topHoldings = buildTopHoldings(calc.holdings());

        return new PortfolioRes(
                calc.userId(),
                calc.depositAmount(),
                calc.totalEvaluationAmount(),
                calc.totalProfitAmount(),
                calc.totalProfitRate(),
                calc.holdings(),
                topHoldings
        );
    }

    /**
     *  2. 계좌 조회 API
     * 보유 종목 전체 + 평가 정보 (상위3 불필요)
     */
    public AccountPortfolioRes getAccountPortfolio(String cano, Long userId) {

        PortfolioCalcResult calc = calculatePortfolio(cano, userId);

        return new AccountPortfolioRes(
                calc.userId(),
                calc.depositAmount(),
                calc.totalEvaluationAmount(),
                calc.totalProfitAmount(),
                calc.totalProfitRate(),
                calc.holdings()
        );
    }

    /**
     * 3. 대시보드 조회 API
     * 보유 종목 상위 3개만
     */
    public DashboardPortfolioRes getDashboardPortfolio(String cano, Long userId) {

        PortfolioCalcResult calc = calculatePortfolio(cano, userId);

        List<HoldingItem> top3 = calc.holdings().stream()
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .limit(3)
                .toList();

        return new DashboardPortfolioRes(
                calc.userId(),
                calc.depositAmount(),
                calc.totalEvaluationAmount(),
                calc.totalProfitAmount(),
                calc.totalProfitRate(),
                top3
        );
    }

    /**
     * 상위 3개 + 기타 비중 계산 로직
     */
    private List<TopHoldingItem> buildTopHoldings(List<HoldingItem> items) {

        List<HoldingItem> sorted = items.stream()
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .toList();

        List<TopHoldingItem> top3 = sorted.stream()
                .limit(3)
                .map(i -> new TopHoldingItem(i.productName(), i.weight()))
                .toList();

        double etcWeight = sorted.stream()
                .skip(3)
                .mapToDouble(HoldingItem::weight)
                .sum();

        if (etcWeight > 0) {
            List<TopHoldingItem> list = new ArrayList<>(top3);
            list.add(new TopHoldingItem("기타", etcWeight));
            return list;
        }

        return top3;
    }
}
