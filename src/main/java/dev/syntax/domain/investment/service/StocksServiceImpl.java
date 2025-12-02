package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.StockDetailRes;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import dev.syntax.domain.investment.repository.InvestPortfolioRepository;
import dev.syntax.external.kis.KisStockApiClient;
import dev.syntax.external.kis.dto.MultiPriceRes;
import dev.syntax.external.kis.dto.PriceItem;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StocksServiceImpl implements StocksService {

    private final KisStockApiClient kisStockApiClient;
    private final InvestAccountRepository investAccountRepository;
    private final InvestPortfolioRepository portfolioRepository;

    @Override
    public MultiPriceRes getStocksForBuy() {
        return kisStockApiClient.getMultiPrice();
    }

    @Override
    public MultiPriceRes getStocksForSell(Long userId) {
        List<String> codes  = portfolioRepository.findProductCodesByUserId(userId);

        if (codes.isEmpty()) {
            return new MultiPriceRes(List.of());
        }

        // 2) KIS 멀티 시세 조회
        MultiPriceRes prices = kisStockApiClient.getPrices(codes);

        return prices;
    }

    @Override
    @Transactional(readOnly = true)
    public StockDetailRes getStockDetail(Long userId, String code) {

        // 1) 현재가 조회 (KIS)
        MultiPriceRes res = kisStockApiClient.getPrices(java.util.List.of(code));
        PriceItem item = res.getOutput().get(0);

        long currentPrice = Long.parseLong(item.getCurrentPrice());

        // 2) 예수금 조회(Core DB)
        InvestAccount account = investAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.ACCOUNT_NOT_FOUND));

        long deposit = account.getDepositAmount();

        // 3) 최대 매수 가능 수량 계산
        int maxBuyQuantity = currentPrice > 0 ? (int)(deposit / currentPrice) : 0;

        // 4) 보유 수량 조회
        long holdingQuantity = portfolioRepository
                .findHoldingQuantity(account.getCano(), code)
                .orElse(0L);

        // 5) DTO 반환
        return StockDetailRes.builder()
                .productCode(item.getProductCode())
                .productName(item.getProductName())
                .currentPrice(currentPrice)
                .prevRate(item.getPrevRate())
                .accumulatedVolume(item.getAccumulatedVolume())
                .depositAmount(deposit)
                .maxBuyQuantity(maxBuyQuantity)
                .holdingQuantity(holdingQuantity)
                .build();
    }
}
