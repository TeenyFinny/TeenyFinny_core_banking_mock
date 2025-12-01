package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BalanceServiceConcurrencyTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CoreUserRepository coreUserRepository;

    // @Test
    // @DisplayName("동시에 100개의 출금 요청이 들어와도 잔액이 음수가 되지 않아야 한다")
    // void withdrawConcurrencyTest() throws InterruptedException {
    //     // given
    //     CoreUser user = coreUserRepository.save(CoreUser.builder()
    //             .name("Test User")
    //             .phoneNumber("010-1234-5678")
    //             .birthDate(LocalDate.of(1990, 1, 1))
    //             .channelUserId(1L)
    //             .build());

    //     Account account = accountRepository.save(Account.builder()
    //             .user(user)
    //             .number("123-456-7890")
    //             .productName("Test Account")
    //             .balance(BigDecimal.valueOf(1000)) // 초기 잔액 1000원
    //             .interestRate(BigDecimal.valueOf(0.1))
    //             .status(AccountStatus.ACTIVE)
    //             .type(AccountType.DEPOSIT)
    //             .build());

    //     int threadCount = 100;
    //     ExecutorService executorService = Executors.newFixedThreadPool(32);
    //     CountDownLatch latch = new CountDownLatch(threadCount);
    //     AtomicInteger successCount = new AtomicInteger();
    //     AtomicInteger failCount = new AtomicInteger();

    //     // when
    //     for (int i = 0; i < threadCount; i++) {
    //         executorService.submit(() -> {
    //             try {
    //                 balanceService.withdraw(
    //                         account.getId(),
    //                         user,
    //                         BigDecimal.valueOf(100), // 100원 출금
    //                         "Concurrency Test",
    //                         TransactionCategory.ETC,
    //                         null,
    //                         TransactionCode.WITHDRAW
    //                 );
    //                 successCount.incrementAndGet();
    //             } catch (Exception e) {
    //                 failCount.incrementAndGet();
    //             } finally {
    //                 latch.countDown();
    //             }
    //         });
    //     }

    //     latch.await();

    //     // then
    //     Account findAccount = accountRepository.findById(account.getId()).orElseThrow();
        
    //     // 1000원 있고 100원씩 출금하므로 10번만 성공해야 함
    //     assertThat(successCount.get()).isEqualTo(10);
    //     assertThat(failCount.get()).isEqualTo(90);
    //     assertThat(findAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    // }
}
