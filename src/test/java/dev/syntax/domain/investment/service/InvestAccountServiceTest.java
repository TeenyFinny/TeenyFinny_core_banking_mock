package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class InvestAccountServiceTest {

    @Mock
    private InvestAccountRepository investAccountRepository;

    @InjectMocks
    private InvestAccountService investAccountService;

    // ============================================================================
    // SUCCESS: 투자 계좌 생성 테스트
    // ============================================================================
    @Test
    @DisplayName("SUCCESS - 투자 계좌 생성 시 예수금이 10000원으로 설정된다")
    void createInvestmentAccount_success() {
        // given
        Long userId = 1L;
        String cano = "123-123";
        Long initialDeposit = 10000L;

        InvestAccount saved = InvestAccount.builder()
                .cano(cano)
                .userId(userId)
                .depositAmount(initialDeposit)
                .build();

        // id 세팅 필요 없음 (cano가 PK)
        given(investAccountRepository.save(any(InvestAccount.class)))
                .willReturn(saved);

        // when
        InvestAccount result =
                investAccountService.createInvestmentAccount(userId, cano, initialDeposit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCano()).isEqualTo(cano);
        assertThat(result.getDepositAmount()).isEqualTo(10000L);  // 예수금 검증

        verify(investAccountRepository).save(any(InvestAccount.class));
    }
    // ============================================================================
    // FAIL: 예수금 초기화 실패 테스트
    // ============================================================================
    @Test
    @DisplayName("FAIL - 저장된 계좌의 예수금이 10000원이 아니면 실패한다")
    void createInvestmentAccount_fail_wrongDepositAmount() {
        // given
        Long userId = 1L;
        String cano = "123-123";

        // 잘못된 초기 예수금으로 저장된 경우
        InvestAccount saved = InvestAccount.builder()
                .cano(cano)
                .userId(userId)
                .depositAmount(0L)
                .build();

        given(investAccountRepository.save(any(InvestAccount.class)))
                .willReturn(saved);

        // when
        InvestAccount result =
                investAccountService.createInvestmentAccount(userId, cano, 10000L);

        // then
        assertThat(result.getDepositAmount()).isNotEqualTo(10000L);
        assertThat(result.getDepositAmount()).isEqualTo(0L);
    }
}
