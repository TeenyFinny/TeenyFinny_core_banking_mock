package dev.syntax.domain.investment.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class InvestAccountServiceTest {

    @Autowired
    private InvestAccountRepository investAccountRepository;

    @Test
    void 투자계좌_생성_성공() {
        // given
        Long userId = 1L;
        Long initialDeposit = 0L;

        // when
        InvestAccountService service = new InvestAccountService(investAccountRepository);
        InvestAccount account = service.createInvestmentAccount(userId, initialDeposit);

        // then
        assertThat(account).isNotNull();
        assertThat(account.getCano()).hasSize(15);   // 15자리 계좌번호
        assertThat(account.getUserId()).isEqualTo(userId);
        assertThat(account.getDepositAmount()).isEqualTo(initialDeposit);

        // DB에 저장됐는지 확인
        InvestAccount savedAccount = investAccountRepository.findById(account.getCano()).orElse(null);
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getUserId()).isEqualTo(userId);
    }
}
