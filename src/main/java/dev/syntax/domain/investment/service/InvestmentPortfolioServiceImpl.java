package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.HoldingItem;
import dev.syntax.domain.investment.dto.StockPrice;
import dev.syntax.domain.investment.dto.TopHoldingItem;
import dev.syntax.domain.investment.dto.cal.PortfolioCalcResult;
import dev.syntax.domain.investment.dto.res.HoldingItemRes;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.DashboardPortfolioRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;
import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.entity.Portfolio;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import dev.syntax.domain.investment.repository.PortfolioRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import dev.syntax.global.service.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InvestmentPortfolioServiceImpl implements InvestmentPortfolioService{

    private final PortfolioRepository portfolioRepository;
    private final InvestmentAccountRepository accountRepository;
    private final StockMarketService stockMarketService;

    /**
     * 공통 포트폴리오 계산 로직
     * 모든 API는 이 계산 결과를 기반으로 응답만 다르게 만든다.
     */
    protected PortfolioCalcResult calculatePortfolio(String cano, Long userId) {

        // 1) 계좌 조회
        InvestmentAccount account = accountRepository.findByCano(cano)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.ACCOUNT_NOT_FOUND));

        // 2) 포트폴리오 로드
        List<Portfolio> portfolios = portfolioRepository.findByCano_Cano(cano);
        // 보유 종목이 전혀 없는 경우
        if (portfolios.isEmpty()) {
            return PortfolioCalcResult.builder()
                    .userId(userId)
                    .depositAmount(account.getDepositAmount())
                    .totalEvaluationAmount(0L)
                    .totalProfitAmount(0L)
                    .totalProfitRate(0.0)
                    .holdings(Collections.emptyList())
                    .build();
        }

        // 보유 종목 리스트
        List<String> productCodes = portfolios.stream().map(Portfolio::getProductCode).toList();

        // 3) 현재가 로드
        Map<String, StockPrice> prices = stockMarketService.getPriceMap(productCodes);

        BigDecimal totalEvalAmount = BigDecimal.ZERO;
        BigDecimal totalPurchaseAmount = BigDecimal.ZERO;     // 총 매입 금액 집계 변수
        List<HoldingItem> items = new ArrayList<>();

        // 4) 종목별 계산
        for (Portfolio p : portfolios) {
            StockPrice priceDto = prices.get(p.getProductCode());
            if (priceDto == null) {
                log.warn("종목코드 '{}'의 현재가 정보를 찾을 수 없어 계산에서 제외합니다.", p.getProductCode());
                continue;
            }

            long currentPrice = priceDto.getCurrentPrice();
            //  모두 BigDecimal로 변경
            BigDecimal qty = BigDecimal.valueOf(p.getHoldingQuantity());
            BigDecimal avg = BigDecimal.valueOf(p.getPurchaseAvgPrice());
            BigDecimal cur = BigDecimal.valueOf(currentPrice);

            //  overflow 가능성 제거
            BigDecimal purchaseAmount = avg.multiply(qty);
            BigDecimal evaluationAmount = cur.multiply(qty);
            BigDecimal profitAmount = evaluationAmount.subtract(purchaseAmount);

            // 수익률 계산 개선 (0 나눔, 반올림)
            double profitRate;
            if (purchaseAmount.compareTo(BigDecimal.ZERO) == 0) {
                profitRate = 0.0;
            } else {
                profitRate = profitAmount
                        .divide(purchaseAmount, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            totalEvalAmount = totalEvalAmount.add(evaluationAmount);
            totalPurchaseAmount = totalPurchaseAmount.add(purchaseAmount);

            items.add(new HoldingItem(
                    p.getProductCode(),
                    p.getProductName(),
                    p.getHoldingQuantity(),
                    p.getPurchaseAvgPrice(),
                    currentPrice,
                    evaluationAmount.longValue(),
                    profitAmount.longValue(),
                    profitRate,
                    0.0 // weight will be calculated next
            ));
        }

        // 5) 총 손익 및 수익률 계산
        BigDecimal totalProfit = totalEvalAmount.subtract(totalPurchaseAmount);
        // 총 수익률 분모는 총 매입 금액 (0 방지)
        double totalProfitRate;
        if (totalPurchaseAmount.compareTo(BigDecimal.ZERO) == 0) {
            totalProfitRate = 0.0;
        } else {
            totalProfitRate = totalProfit
                    .divide(totalPurchaseAmount, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // 6) 비중 계산
        BigDecimal totalEvalDenominator =
                (totalEvalAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalEvalAmount);

        List<HoldingItem> updatedItems = items.stream()
                .map(h -> {
                    BigDecimal evalAmt = BigDecimal.valueOf(h.evaluationAmount());
                    double weight = evalAmt
                            .divide(totalEvalDenominator, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

                    return new HoldingItem(
                            h.productCode(),
                            h.productName(),
                            h.quantity(),
                            h.avgPrice(),
                            h.currentPrice(),
                            h.evaluationAmount(),
                            h.profitAmount(),
                            h.profitRate(),
                            weight
                    );
                })
                .toList();

        // 최종 계산 결과 전달
        return PortfolioCalcResult.builder()
                .userId(userId)
                .depositAmount(account.getDepositAmount())
                .totalEvaluationAmount(totalEvalAmount.longValue())
                .totalProfitAmount(totalProfit.longValue())
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
        List<HoldingItemRes> holdings = convertToRes(calc.holdings());

        List<TopHoldingItem> topHoldings = buildTopHoldings(calc.holdings());

        return new PortfolioRes(
                calc.userId(),
                Utils.NumberFormattingService(calc.depositAmount()),
                Utils.NumberFormattingService(calc.totalEvaluationAmount()),
                Utils.NumberFormattingService(calc.totalProfitAmount()),
                Utils.FormatToTwoDecimal(calc.totalProfitRate()),
                holdings,
                topHoldings
        );
    }

    /**
     *  2. 계좌 조회 API
     * 보유 종목 전체 + 평가 정보 (상위3 불필요)
     */
    public InvestAccountPortfolioRes getAccountPortfolio(String cano, Long userId) {

        PortfolioCalcResult calc = calculatePortfolio(cano, userId);

        return new InvestAccountPortfolioRes(
                cano,
                calc.userId(),
                Utils.NumberFormattingService(calc.depositAmount()),
                Utils.NumberFormattingService(calc.totalEvaluationAmount()),
                Utils.NumberFormattingService(calc.totalProfitAmount()),
                Utils.FormatToTwoDecimal(calc.totalProfitRate()),
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
        List<HoldingItemRes> resTop3 = convertToRes(top3);

        return new DashboardPortfolioRes(
                calc.userId(),
                Utils.NumberFormattingService(calc.depositAmount()),
                Utils.NumberFormattingService(calc.totalEvaluationAmount()),
                Utils.NumberFormattingService(calc.totalProfitAmount()),
                Utils.FormatToTwoDecimal(calc.totalProfitRate()),
                resTop3
        );
    }

    /**
     * 상위 3개 + 기타 비중 계산 로직
     */
    private List<TopHoldingItem> buildTopHoldings(List<HoldingItem> items) {
        if (items.isEmpty()) {
            return List.of(); // 보유 종목 없음
        }

        List<HoldingItem> sorted = items.stream()
                .sorted((a, b) -> Double.compare(b.weight(), a.weight()))
                .toList();

        List<TopHoldingItem> result = new ArrayList<>();
        // 1개, 2개, 3개까지만 안전하게 추가
        int max = Math.min(3, sorted.size());

        for (int i = 0; i < max; i++) {
            result.add(new TopHoldingItem(
                    sorted.get(i).productName(),
                    sorted.get(i).weight()
            ));
        }

        // 기타 처리
        if (sorted.size() > 3) {
            double etc = sorted.stream()
                    .skip(3)
                    .mapToDouble(HoldingItem::weight)
                    .sum();
            result.add(new TopHoldingItem("기타", etc));
        }

        return result;
    }

    /**
     * 금액 숫자 -> String으로 변환하여 res dto로 변환
     */
    private List<HoldingItemRes> convertToRes(List<HoldingItem> items) {
        return items.stream()
                .map(h -> HoldingItemRes.builder()
                        .productCode(h.productCode())
                        .productName(h.productName())
                        .quantity(Utils.NumberFormattingService(h.quantity()))
                        .avgPrice(Utils.NumberFormattingService(h.avgPrice()))
                        .currentPrice(Utils.NumberFormattingService(h.currentPrice()))
                        .evaluationAmount(Utils.NumberFormattingService(h.evaluationAmount()))
                        .profitAmount(Utils.NumberFormattingService(h.profitAmount()))
                        .profitRate(Utils.FormatToTwoDecimal(h.profitRate()))
                        .weight(Utils.RoundToTwoDecimal(h.weight()))
                        .build()
                )
                .toList();
    }
}
