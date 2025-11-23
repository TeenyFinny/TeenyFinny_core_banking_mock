package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
