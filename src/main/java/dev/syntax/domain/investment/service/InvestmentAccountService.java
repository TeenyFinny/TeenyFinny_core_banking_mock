package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentAccountService {

    private final InvestmentAccountRepository investmentAccountRepository;

    /**
     * 투자계좌 생성
     *
     * @param userId 유저 ID
     * @param initialDeposit 초기 예수금
     * @return 생성된 투자계좌
     */
    @Transactional
    public InvestmentAccount createInvestmentAccount(Long userId, Long initialDeposit) {
        String cano = AccountNumberGenerator.generate(); // 기존 유틸 함수 사용

        InvestmentAccount account = InvestmentAccount.builder()
                .cano(cano)
                .userId(userId)
                .depositAmount(initialDeposit)
                .build();

        return investmentAccountRepository.save(account);
    }

}
