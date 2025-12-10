package dev.syntax.domain.investment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.entity.InvestPortfolio;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import dev.syntax.domain.investment.repository.InvestPortfolioRepository;
import dev.syntax.domain.investment.repository.InvestTradeOrderRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InvestTradeOrderServiceImplTest {

    @InjectMocks
    private InvestTradeOrderServiceImpl tradeService;

    @Mock
    private InvestTradeOrderRepository orderRepository;

    @Mock
    private InvestPortfolioRepository portfolioRepository;

    @Mock
    private InvestAccountRepository investAccountRepository;

    @Mock
    private AccountRepository coreAccountRepository;

    // =====================================================================================
    // getAccount() FAIL 케이스 통합
    // =====================================================================================

    @Test
    @DisplayName("FAIL - 계좌가 존재하지 않으면 ACCOUNT_NOT_FOUND 예외 발생한다")
    void getAccount_fail_accountNotFound() {
        // given
        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                tradeService.buy("12345678", 1L, "005930", "삼성전자", 1, 70000)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("FAIL - 계좌는 있으나 userId 불일치 시 ACCOUNT_NOT_FOUND 예외 발생한다")
    void getAccount_fail_userIdMismatch() {
        // given
        InvestAccount wrongUserAccount = InvestAccount.builder()
                .cano("12345678")
                .userId(999L) // userId 다름
                .depositAmount(100000L)
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(wrongUserAccount));

        // when & then
        assertThatThrownBy(() ->
                tradeService.buy("12345678", 1L, "005930", "삼성전자", 1, 70000)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.ACCOUNT_NOT_FOUND.getMessage());
    }

    // =====================================================================================
    // 매수(BUY) 성공 - 예수금 충분할 때 EXECUTED 주문 생성
    // =====================================================================================
    @Test
    @DisplayName("SUCCESS - 매수: 예수금이 충분하면 EXECUTED 주문 생성된다")
    void buy_success() {

        // given
        InvestAccount account = InvestAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(1_000_000L)   // 충분한 예수금
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(account));

        // 포트폴리오 존재 X → 신규 생성
        given(portfolioRepository.findByCano_CanoAndProductCode(anyString(), anyString()))
                .willReturn(Optional.empty());

        // shadow account
        Account core = Account.builder()
                .type(AccountType.INVESTMENT)
                .balance(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(core, "id", 99L);

        given(coreAccountRepository.findByUserIdWithPessimisticLock(1L, AccountType.INVESTMENT))
                .willReturn(Optional.of(core));

        given(orderRepository.save(any(TradeOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        TradeOrder order = tradeService.buy(
                "12345678",
                1L,
                "005930",
                "삼성전자",
                10,
                50000
        );

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(order.getQuantity()).isEqualTo(10);
        assertThat(order.getPrice()).isEqualTo(50000);
    }

    // =====================================================================================
    // 매수 FAIL - 예수금 부족
    // =====================================================================================
    @Test
    @DisplayName("FAIL - 매수: 예수금 부족 시 주문이 실패한다(INSUFFICIENT_BALANCE)")
    void buy_fail_insufficientBalance() {
        // given
        InvestAccount account = InvestAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(10_000L) // 부족한 예수금
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(account));

        // when & then
        assertThatThrownBy(() -> tradeService.buy(
                "12345678", 1L, "005930", "삼성전자", 10, 50000
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.INSUFFICIENT_BALANCE.getMessage());
    }

    // =====================================================================================
    // 매도(SELL) 성공 - 보유 10주에서 매도
    // =====================================================================================
    @Test
    @DisplayName("SUCCESS - 매도: 보유 수량 내에서 매도하면 주문 생성된다")
    void sell_success() {
        // given
        InvestAccount account = InvestAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(10000L)
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(account));

        InvestPortfolio portfolio = InvestPortfolio.builder()
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(10L) // 보유 10주
                .purchaseAvgPrice(50000L)
                .build();

        given(portfolioRepository.findByCano_CanoAndProductCode("12345678", "005930"))
                .willReturn(Optional.of(portfolio));

        // shadow account
        Account core = Account.builder()
                .type(AccountType.INVESTMENT)
                .balance(BigDecimal.ZERO)
                .build();
        given(coreAccountRepository.findByUserIdWithPessimisticLock(1L, AccountType.INVESTMENT))
                .willReturn(Optional.of(core));

        given(orderRepository.save(any(TradeOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        TradeOrder order = tradeService.sell(
                "12345678", 1L,
                "005930", "삼성전자",
                5, 50000
        );

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(order.getQuantity()).isEqualTo(5);
    }

    // =====================================================================================
    // 매도 FAIL - 보유수량 초과 매도
    // =====================================================================================
    @Test
    @DisplayName("FAIL - 보유량 초과 매도 시 에러가 발생한다.(INSUFFICIENT_HOLDING)")
    void sell_fail_insufficientHolding() {
        // given
        InvestAccount account = InvestAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(10000L)
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(account));

        // 보유 10주
        InvestPortfolio portfolio = InvestPortfolio.builder()
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(10L)
                .purchaseAvgPrice(50000L)
                .build();

        given(portfolioRepository.findByCano_CanoAndProductCode("12345678", "005930"))
                .willReturn(Optional.of(portfolio));

        // when & then
        assertThatThrownBy(() ->
                tradeService.sell("12345678", 1L, "005930", "삼성전자", 20, 50000))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.INSUFFICIENT_HOLDING.getMessage());
    }

    // =====================================================================================
    // 매도 - 보유수량이 0이면 포트폴리오 삭제
    // =====================================================================================
    @Test
    @DisplayName("SUCCESS - 매도 후 보유수량이 0이면 포트폴리오 삭제(delete)된다")
    void sell_portfolioDeletedWhenQuantityZero() {
        // given
        InvestAccount account = InvestAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(10000L)
                .build();

        given(investAccountRepository.findWithLockByCano("12345678"))
                .willReturn(Optional.of(account));

        // 현재 보유수량 = 5주
        InvestPortfolio portfolio = InvestPortfolio.builder()
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(5L)
                .purchaseAvgPrice(50000L)
                .build();

        given(portfolioRepository.findByCano_CanoAndProductCode("12345678", "005930"))
                .willReturn(Optional.of(portfolio));

        // shadow account
        Account core = Account.builder()
                .type(AccountType.INVESTMENT)
                .balance(BigDecimal.ZERO)
                .build();
        given(coreAccountRepository.findByUserIdWithPessimisticLock(1L, AccountType.INVESTMENT))
                .willReturn(Optional.of(core));

        given(orderRepository.save(any(TradeOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when - 5주 모두 매도 → 보유수량 = 0
        tradeService.sell("12345678", 1L, "005930", "삼성전자", 5, 50000);

        // then
        verify(portfolioRepository, times(1)).delete(portfolio);  // 삭제됐는지 확인
        verify(portfolioRepository, never()).save(any());         // save()는 호출되면 안됨
    }

    @Test
    @DisplayName("price가 0 이하이면 INVALID_ORDER 예외가 발생해야 한다")
    void buy_fail_priceLessThanOrZero_beforeFix() {
        assertThatThrownBy(() ->
                tradeService.buy("12345678", 1L, "005930", "삼성전자", 10, 0)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.INVALID_ORDER.getMessage());
    }
}
