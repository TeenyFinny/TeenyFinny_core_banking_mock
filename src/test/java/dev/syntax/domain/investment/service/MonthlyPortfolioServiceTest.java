package dev.syntax.domain.investment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import dev.syntax.domain.investment.dto.TopHoldingItem;
import dev.syntax.domain.investment.dto.res.HoldingItemRes;
import dev.syntax.domain.investment.dto.res.InvestPortfolioRes;
import dev.syntax.domain.investment.entity.InvestPortfolioMonthly;
import dev.syntax.domain.investment.entity.InvestPortfolioMonthlySummary;
import dev.syntax.domain.investment.repository.InvestPortfolioMonthlyRepository;
import dev.syntax.domain.investment.repository.InvestPortfolioMonthlySummaryRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyPortfolioServiceTest {

    @InjectMocks
    private MonthlyPortfolioService monthlyService;

    @Mock
    private InvestPortfolioMonthlySummaryRepository summaryRepo;

    @Mock
    private InvestPortfolioMonthlyRepository monthlyRepo;

    // =========================================================================================
    // SUCCESS : 요약 + 보유 종목 리스트 조회 성공
    // =========================================================================================
    @Test
    @DisplayName("SUCCESS - 월간 포트폴리오 조회 성공 시 InvestPortfolioRes 반환된다")
    void getMonthlyPortfolio_success() {

        // given
        String cano = "12345678";
        Long userId = 1L;

        // Summary Mock
        InvestPortfolioMonthlySummary summary = InvestPortfolioMonthlySummary.builder()
                .cano(cano)
                .userId(userId)
                .year(2025)
                .month(1)
                .depositAmount(1_000_000L)
                .totalEvaluationAmount(1_200_000L)
                .totalProfitAmount(200_000L)
                .totalProfitRate(20.0)
                .top1Name("삼성전자")
                .top1Weight(50.0)
                .top2Name("카카오")
                .top2Weight(30.0)
                .top3Name("네이버")
                .top3Weight(10.0)
                .etcWeight(10.0)
                .build();

        given(summaryRepo.findByCanoAndUserIdAndYearAndMonth(cano, userId, 2025, 1))
                .willReturn(Optional.of(summary));

        // Monthly Items Mock
        InvestPortfolioMonthly item1 = InvestPortfolioMonthly.builder()
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(10L)
                .purchaseAvgPrice(50000L)
                .currentPrice(60000L)
                .evaluationAmount(600000L)
                .profitAmount(100000L)
                .profitRate(20.0)
                .weight(50.0)
                .build();

        InvestPortfolioMonthly item2 = InvestPortfolioMonthly.builder()
                .productCode("035720")
                .productName("카카오")
                .holdingQuantity(5L)
                .purchaseAvgPrice(70000L)
                .currentPrice(80000L)
                .evaluationAmount(400000L)
                .profitAmount(50000L)
                .profitRate(12.5)
                .weight(30.0)
                .build();

        given(monthlyRepo.findAllByCanoAndUserIdAndYearAndMonth(cano, userId, 2025, 1))
                .willReturn(List.of(item1, item2));

        // when
        InvestPortfolioRes res = monthlyService.getMonthlyPortfolio(cano, userId, 2025, 1);

        // then
        assertThat(res).isNotNull();
        assertThat(res.userId()).isEqualTo(userId);

        // holdings 검증
        assertThat(res.holdings()).hasSize(2);

        HoldingItemRes holding1 = res.holdings().get(0);
        assertThat(holding1.productName()).isEqualTo("삼성전자");

        // top 보유 비중 검증
        assertThat(res.topHoldings()).hasSize(4); // top1, top2, top3, 기타

        TopHoldingItem top1 = res.topHoldings().get(0);
        assertThat(top1.productName()).isEqualTo("삼성전자");
        assertThat(top1.weight()).isEqualTo(50.0);
    }

    // =========================================================================================
    // FAIL : summary 가 없을 때 PORTFOLIO_NOT_FOUND 예외 발생
    // =========================================================================================
    @Test
    @DisplayName("FAIL - Summary 데이터가 없으면 PORTFOLIO_NOT_FOUND 예외 발생")
    void getMonthlyPortfolio_fail_summaryNotFound() {

        // given
        String cano = "12345678";
        Long userId = 1L;

        given(summaryRepo.findByCanoAndUserIdAndYearAndMonth(cano, userId, 2025, 1))
                .willReturn(Optional.empty()); //  summary 없는 경우

        // when & then
        assertThatThrownBy(() -> monthlyService.getMonthlyPortfolio(cano, userId, 2025, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.PORTFOLIO_NOT_FOUND.getMessage());
    }
}
