package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.TopHoldingItem;
import dev.syntax.domain.investment.dto.res.HoldingItemRes;
import dev.syntax.domain.investment.dto.res.InvestPortfolioRes;
import dev.syntax.domain.investment.entity.InvestPortfolioMonthly;
import dev.syntax.domain.investment.entity.InvestPortfolioMonthlySummary;
import dev.syntax.domain.investment.repository.InvestPortfolioMonthlyRepository;
import dev.syntax.domain.investment.repository.InvestPortfolioMonthlySummaryRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import dev.syntax.global.service.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyPortfolioService {

    private final InvestPortfolioMonthlyRepository monthlyRepo;
    private final InvestPortfolioMonthlySummaryRepository summaryRepo;

    public InvestPortfolioRes getMonthlyPortfolio(String cano, Long userId, int year, int month) {

        InvestPortfolioMonthlySummary summary = summaryRepo
                .findByCanoAndUserIdAndYearAndMonth(cano, userId, year, month)
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.PORTFOLIO_NOT_FOUND));

        List<InvestPortfolioMonthly> items = monthlyRepo
                .findAllByCanoAndUserIdAndYearAndMonth(cano, userId, year, month);

        List<HoldingItemRes> holdings = items.stream()
                .map(i -> new HoldingItemRes(
                        i.getProductCode(),
                        i.getProductName(),
                        Utils.NumberFormattingService(i.getHoldingQuantity()),
                        Utils.NumberFormattingService(i.getPurchaseAvgPrice()),
                        Utils.NumberFormattingService(i.getCurrentPrice()),
                        Utils.NumberFormattingService(i.getEvaluationAmount()),
                        Utils.NumberFormattingService(i.getProfitAmount()),
                        Utils.FormatToTwoDecimal(i.getProfitRate()),
                        Utils.RoundToTwoDecimal(i.getWeight())
                ))
                .toList();

        List<TopHoldingItem> top = buildTop(summary);

        return new InvestPortfolioRes(
                summary.getUserId(),
                Utils.NumberFormattingService(summary.getDepositAmount()),
                Utils.NumberFormattingService(summary.getTotalEvaluationAmount()),
                Utils.NumberFormattingService(summary.getTotalProfitAmount()),
                Utils.FormatToTwoDecimal(summary.getTotalProfitRate()),
                holdings,
                top
        );
    }

    private List<TopHoldingItem> buildTop(InvestPortfolioMonthlySummary summary) {
        List<TopHoldingItem> list = new ArrayList<>();

        if (summary.getTop1Name() != null)
            list.add(new TopHoldingItem(summary.getTop1Name(), summary.getTop1Weight()));

        if (summary.getTop2Name() != null)
            list.add(new TopHoldingItem(summary.getTop2Name(), summary.getTop2Weight()));

        if (summary.getTop3Name() != null)
            list.add(new TopHoldingItem(summary.getTop3Name(), summary.getTop3Weight()));

        if (summary.getEtcWeight() != null)
            list.add(new TopHoldingItem("기타", summary.getEtcWeight()));

        return list;
    }
}
