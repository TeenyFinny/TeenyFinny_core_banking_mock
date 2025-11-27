package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestAccountService {

    private final InvestAccountRepository investAccountRepository;

    /**
     * 투자계좌 생성
     *
     * @param userId 유저 ID
     * @param initialDeposit 초기 예수금
     * @return 생성된 투자계좌
     */
    @Transactional
    public InvestAccount createInvestmentAccount(Long userId, Long initialDeposit) {

        InvestAccount account = InvestAccount.builder()
                .cano(AccountNumberGenerator.generate())
                .userId(userId)
                .depositAmount(initialDeposit)
                .build();

        return investAccountRepository.save(account);
    }
}
