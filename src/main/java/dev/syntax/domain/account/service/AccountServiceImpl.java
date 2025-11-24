package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.user.entity.CoreUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountItemRes> getUserAccounts(Long coreUserId) {
        List<Account> accounts = accountRepository.findAllByUserId(coreUserId);

        return accounts.stream()
                .map(AccountItemRes::from)
                .toList();
    }

    @Transactional
    @Override
    public Account createDepositAccount(CoreUser user) {
        Account account = Account.builder()
                .user(user)
                .number(AccountNumberGenerator.generate())
                .productName("입출금 통장")
                .interestRate(new BigDecimal("0.001")) // 0.1%
                .type(AccountType.DEPOSIT)
                .build();

        return accountRepository.save(account);
    }
}
