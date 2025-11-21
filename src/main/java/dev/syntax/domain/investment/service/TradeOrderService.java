package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.entity.Portfolio;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import dev.syntax.domain.investment.repository.PortfolioRepository;
import dev.syntax.domain.investment.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TradeOrderService {

    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioRepository portfolioRepository;
    private final InvestmentAccountRepository accountRepository;

    /**
     * 매수 주문 처리
     *
     * - baas_investment_account.dnca_tot_amt(예수금) 차감
     * - baas_portfolio.hldg_qty, pchs_avg_pric 갱신
     * - baas_trade_orders에 주문 내역 기록
     */
    public TradeOrder buy(
            String cano,          // CHAR(8)
            Long userId,          // BIGINT
            String productCode,   // pdno
            String productName,   // prdt_name
            int quantity,         // ord_qty (INT)
            long price            // ord_unpr (BIGINT)
    ) {
        InvestmentAccount account = getAccount(cano, userId);

        long totalCost = (long) quantity * price; // 주문 금액 = 수량 * 단가

        // 1. 예수금 부족 체크 (dnca_tot_amt)
        if (account.getDepositAmount() < totalCost) {
            throw new IllegalArgumentException("예수금이 부족합니다.");
        }

        // 2. 포트폴리오 조회 또는 신규 생성 (baas_portfolio)
        Portfolio portfolio = portfolioRepository
                .findByCano_CanoAndProductCode(cano, productCode)
                .orElseGet(() -> Portfolio.builder()
                        .cano(account)
                        .userId(userId)
                        .productCode(productCode)
                        .productName(productName)
                        .holdingQuantity(0L)
                        .purchaseAvgPrice(0L)
                        .build()
                );

        // 3. 보유수량/평균매입단가 갱신 (hldg_qty, pchs_avg_pric)
        portfolio.updateHolding((long) quantity, price);
        portfolioRepository.save(portfolio);

        // 4. 예수금 차감 (dnca_tot_amt)
        account.withdraw(totalCost);
        accountRepository.save(account);

        // 5. 거래내역 저장 (baas_trade_orders)
        TradeOrder order = TradeOrder.builder()
                .cano(account)
                .userId(userId)
                .globalUid(null)              // 필요하면 외부에서 세팅
                .orderTime(LocalDateTime.now())
                .tradeType(TradeType.TTTTC0012U)     // tr_id ENUM
                .productCode(productCode)
                .productName(productName)
                .quantity(quantity)
                .price(price)
                .exchangeDivisionCode("KRX")  // excg_id_dvsn_cd, 필요시 파라미터로
                .status(OrderStatus.REQUESTED)
                .build();

        return tradeOrderRepository.save(order);
    }

    /**
     * 매도 주문 처리
     *
     * - baas_portfolio.hldg_qty 감소
     * - baas_investment_account.dnca_tot_amt(예수금) 증가
     * - baas_trade_orders에 주문 내역 기록
     */
    public TradeOrder sell(
            String cano,
            Long userId,
            String productCode,
            String productName,
            int quantity,
            long price
    ) {
        InvestmentAccount account = getAccount(cano, userId);

        // 1. 포트폴리오 존재/보유수량 확인
        Portfolio portfolio = portfolioRepository
                .findByCano_CanoAndProductCode(cano, productCode)
                .orElseThrow(() -> new IllegalArgumentException("보유한 종목이 없습니다."));

        if (portfolio.getHoldingQuantity() < quantity) {
            throw new IllegalArgumentException("보유수량이 부족합니다.");
        }

        // 2. 보유수량 감소
        portfolio.reduceHolding((long) quantity);
        portfolioRepository.save(portfolio);

        // 3. 예수금 증가 (매도 금액 만큼 dnca_tot_amt 증가)
        long revenue = (long) quantity * price;
        account.deposit(revenue);
        accountRepository.save(account);

        // 4. 거래내역 저장
        TradeOrder order = TradeOrder.builder()
                .cano(account)
                .userId(userId)
                .globalUid(null)
                .orderTime(LocalDateTime.now())
                .tradeType(TradeType.TTTCO011U)
                .productCode(productCode)
                .productName(productName)
                .quantity(quantity)
                .price(price)
                .exchangeDivisionCode("KRX")
                .status(OrderStatus.REQUESTED)
                .build();

        return tradeOrderRepository.save(order);
    }

    /**
     * 계좌 존재 여부 + user_id 검증
     */
    private InvestmentAccount getAccount(String cano, Long userId) {
        return accountRepository.findById(cano)
                .filter(acc -> acc.getUserId().equals(userId))
                .orElseThrow(() ->
                        new IllegalArgumentException("계좌가 존재하지 않거나 사용자 정보가 일치하지 않습니다.")
                );
    }
}
