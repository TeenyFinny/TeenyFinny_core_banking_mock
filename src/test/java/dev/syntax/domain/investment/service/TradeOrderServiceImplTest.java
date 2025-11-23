package dev.syntax.domain.investment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.entity.Portfolio;
import dev.syntax.domain.investment.entity.TradeOrder;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import dev.syntax.domain.investment.repository.PortfolioRepository;
import dev.syntax.domain.investment.repository.TradeOrderRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorInvestmentCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @Mock
    private TradeOrderRepository tradeOrderRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private InvestmentAccountRepository accountRepository;

    @InjectMocks
    private TradeOrderServiceImpl service;

    private InvestmentAccount account;

    @BeforeEach
    void setUp() {
        account = InvestmentAccount.builder()
                .cano("12345678")
                .userId(1L)
                .depositAmount(1_000_000L)
                .build();
    }

    // -----------------------------------------------------
    @Test
    @DisplayName("매수 성공 테스트")
    void buy_success() {
        String cano = "12345678";
        Long userId = 1L;

        when(accountRepository.findById(cano))
                .thenReturn(Optional.of(account));

        when(portfolioRepository.findByCano_CanoAndProductCode(cano, "005930"))
                .thenReturn(Optional.empty());

        Portfolio savedPortfolio = Portfolio.builder()
                .id(1L)
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(10L)
                .purchaseAvgPrice(10000L)
                .build();

        when(portfolioRepository.save(any())).thenReturn(savedPortfolio);

        TradeOrder savedOrder = TradeOrder.builder()
                .id(1L)
                .cano(account)
                .userId(1L)
                .tradeType(TradeType.TTTTC0012U)
                .productCode("005930")
                .productName("삼성전자")
                .quantity(10)
                .price(10000L)
                .exchangeDivisionCode("KRX")
                .status(OrderStatus.REQUESTED)
                .build();

        when(tradeOrderRepository.save(any())).thenReturn(savedOrder);

        TradeOrder result = service.buy(
                cano, 1L,
                "005930", "삼성전자",
                10, 10000L
        );

        assertThat(result).isNotNull();
        assertThat(result.getProductCode()).isEqualTo("005930");
        assertThat(account.getDepositAmount()).isEqualTo(900_000L);

        verify(portfolioRepository).save(any());
        verify(tradeOrderRepository).save(any());
    }

    // -----------------------------------------------------
    @Test
    @DisplayName("매수 실패 — 예수금 부족")
    void buy_fail_insufficient_balance() {
        String cano = "12345678";
        when(accountRepository.findById((cano)))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.buy(
                cano, 1L,
                "005930", "삼성전자",
                10, 200000L
        )).isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.INSUFFICIENT_BALANCE.getMessage());

    }

    // -----------------------------------------------------
    @Test
    @DisplayName("매도 성공 테스트")
    void sell_success() {
        String cano = "12345678";

        // 계좌 조회 시 moc account 반환
        when(accountRepository.findById(cano)).thenReturn(Optional.of(account));

        // mock portfolio 생성
        Portfolio portfolio = Portfolio.builder()
                .id(1L)
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(10L)
                .purchaseAvgPrice(10000L)
                .build();

        // 포트폴리오 find 시 mock portfolio 반환
        when(portfolioRepository.findByCano_CanoAndProductCode(cano,"005930"))
                .thenReturn(Optional.of(portfolio));


        // 보유수량 감소 save(portfolio) 시 moc account 반환
        when(portfolioRepository.save(any()))
                .thenReturn(portfolio);

        // 예수금 증가 save(portfolio) 시 moc account 반환
        when(tradeOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));


        // sell() 호출 후 결과 비교
        TradeOrder result = service.sell(
                cano, 1L,
                "005930", "삼성전자",
                1, 5000L
        );

        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isEqualTo("삼성전자");
        assertThat(account.getDepositAmount()).isEqualTo(1_005_000L);
        assertThat(portfolio.getHoldingQuantity()).isEqualTo(9L);


    }

    // -----------------------------------------------------
    @Test
    @DisplayName("매도 실패 — 보유수량 부족")
    void sell_fail_insufficient_holding() {
        String cano = "12345678";

        when(accountRepository.findById(cano))
                .thenReturn(Optional.of(account));

        Portfolio portfolio = Portfolio.builder()
                .id(1L)
                .cano(account)
                .userId(1L)
                .productCode("005930")
                .productName("삼성전자")
                .holdingQuantity(3L) // 부족함
                .purchaseAvgPrice(10000L)
                .build();

        when(portfolioRepository.findByCano_CanoAndProductCode(cano, "005930"))
                .thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() ->
                service.sell(cano, 1L, "005930", "삼성전자", 10, 20000L)
        ).isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.INSUFFICIENT_HOLDING.getMessage());
    }

    // -----------------------------------------------------
    @Test
    @DisplayName("계좌 존재 X")
    void fail_account_not_found() {
        when(accountRepository.findById("12345678"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                service.buy("12345678", 1L, "005930", "삼성전자", 1, 50000L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorInvestmentCode.ACCOUNT_NOT_FOUND.getMessage());
    }

}
