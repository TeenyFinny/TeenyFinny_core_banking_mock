package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.entity.InvestPortfolio;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import dev.syntax.domain.investment.repository.InvestPortfolioRepository;
import dev.syntax.domain.investment.repository.InvestTradeOrderRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestTradeOrderServiceImpl implements InvestTradeOrderService {

    private final InvestTradeOrderRepository investTradeOrderRepository;
    private final InvestPortfolioRepository investPortfolioRepository;
    private final InvestAccountRepository investAccountRepository;
    private final AccountRepository coreAccountRepository;

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
            long quantity,         // ord_qty (BIGINT)
            long price            // ord_unpr (BIGINT)
    ) {
        InvestAccount account = getAccount(cano, userId);

        long totalCost = quantity * price; // 주문 금액 = 수량 * 단가

        // 1. 예수금 부족 체크 (dnca_tot_amt)
        if (account.getDepositAmount() < totalCost) {
            throw new BusinessException(ErrorInvestmentCode.INSUFFICIENT_BALANCE);
        }

        // 2. 포트폴리오 조회 또는 신규 생성 (baas_portfolio)
        InvestPortfolio portfolio = investPortfolioRepository
                .findByCano_CanoAndProductCode(cano, productCode)
                .orElseGet(() -> InvestPortfolio.builder()
                        .cano(account)
                        .userId(userId)
                        .productCode(productCode)
                        .productName(productName)
                        .holdingQuantity(0L)
                        .purchaseAvgPrice(0L)
                        .build()
                );

        // 3. 보유수량/평균매입단가 갱신 (hldg_qty, pchs_avg_pric)
        portfolio.updateHolding(quantity, price);
        investPortfolioRepository.save(portfolio);

        // 4. 예수금 차감 (dnca_tot_amt)
        account.withdraw(totalCost);

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
                .status(OrderStatus.EXECUTED)
                .build();

        syncShadowAccount(userId, account);

        return investTradeOrderRepository.save(order);
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
            long quantity,
            long price
    ) {
        InvestAccount account = getAccount(cano, userId);

        // 1. 포트폴리오 존재/보유수량 확인
        InvestPortfolio portfolio = investPortfolioRepository
                .findByCano_CanoAndProductCode(cano, productCode)
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.STOCK_NOT_FOUND));

        if (portfolio.getHoldingQuantity() < quantity) {
            throw new BusinessException(ErrorInvestmentCode.INSUFFICIENT_HOLDING);
        }

        // 2. 보유수량 감소
        portfolio.reduceHolding(quantity);

        // 3. 보유수량이 0이면 삭제
        if (portfolio.getHoldingQuantity() == 0) {
            investPortfolioRepository.delete(portfolio);
        } else {
            investPortfolioRepository.save(portfolio); // 0이 아닐 때만 업데이트
        }

        // 4. 예수금 증가 (매도 금액 만큼 dnca_tot_amt 증가)
        long revenue = quantity * price;
        account.deposit(revenue);

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
                .status(OrderStatus.EXECUTED)
                .build();

        syncShadowAccount(userId, account);

        return investTradeOrderRepository.save(order);
    }

    /**
     * 계좌 존재 여부 + user_id 검증
     */
    private InvestAccount getAccount(String cano, Long userId) {
        return investAccountRepository.findWithLockByCano(cano)
                .filter(acc -> acc.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorInvestmentCode.ACCOUNT_NOT_FOUND));
    }

    @Transactional
    public void syncShadowAccount(Long userId, InvestAccount investAccount) {
        Account core = coreAccountRepository.findByUserIdWithPessimisticLock(userId, AccountType.INVESTMENT).get();
        core.setBalance(BigDecimal.valueOf(investAccount.getDepositAmount()));
    }
}