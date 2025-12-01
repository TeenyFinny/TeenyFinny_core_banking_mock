package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.dto.TransactionAllowanceHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionDetailItemRes;
import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.transaction.service.TransactionService;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Rollback(false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BalanceServiceConcurrencyTest {

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CoreUserRepository coreUserRepository;

    // /**
    //  * 테스트용 TransactionService — 실제 기능 없음 (빈 충돌 방지)
    //  */
    // @TestConfiguration
    // static class TestConfig {
    //     @Bean
    //     public TransactionService transactionService() {
    //         return new TransactionService() {
    //             @Override
    //             public void record(
    //                     CoreUser user,
    //                     Account account,
    //                     TransactionType type,
    //                     BigDecimal amount,
    //                     BigDecimal balanceAfter,
    //                     String merchantName,
    //                     TransactionCategory category,
    //                     TransactionStatus status,
    //                     TransactionCode code
    //             ) {
    //             }

    //             @Override
    //             public TransactionHistoryRes getHistory(String number) {
    //                 return null;
    //             }

    //             @Override
    //             public TransactionAllowanceHistoryRes getHistoryByMonth(String number, int year, int month) {
    //                 return null;
    //             }

    //             @Override
    //             public TransactionDetailItemRes getTransactionDetail(Long transactionId) {
    //                 return null;
    //             }
    //         };
    //     }
    // }

    @Test
    @DisplayName("동시에 100개 출금해도 성공 10, 실패 90, 잔액 0 보장")
    void withdrawConcurrencyTest() throws InterruptedException {

        // given
        CoreUser user = coreUserRepository.save(CoreUser.builder()
                .name("Test User")
                .phoneNumber("010-1234-1234")
                .birthDate(LocalDate.of(1990, 1, 1))
                .channelUserId(1L)
                .build());

        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Account account = accountRepository.save(Account.builder()
                .user(user)
                .number(shortUuid)
                .productName("Test Account")
                .balance(BigDecimal.valueOf(1000))
                .interestRate(BigDecimal.valueOf(0.1))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.DEPOSIT)
                .build());

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    balanceService.withdraw(
                            account.getId(),
                            user,
                            BigDecimal.valueOf(100),
                            "Concurrency Test",
                            TransactionCategory.ETC,
                            null,
                            TransactionCode.WITHDRAW
                    );
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Account updated = accountRepository.findById(account.getId()).orElseThrow();

        // then
        assertThat(success.get()).isEqualTo(10);
        assertThat(fail.get()).isEqualTo(90);
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}