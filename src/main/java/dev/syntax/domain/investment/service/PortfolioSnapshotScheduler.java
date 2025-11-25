package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.repository.InvestmentAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioSnapshotScheduler {

    private final InvestmentAccountRepository accountRepository;
    private final PortfolioSnapshotService snapshotService;

    @Scheduled(cron = "0 5 0 1 * *") // 매월 1일 00:05
    public void runMonthlySnapshot() {

        LocalDate now = LocalDate.now().minusMonths(1);
        int year = now.getYear();
        int month = now.getMonthValue();

        List<InvestmentAccount> accounts = accountRepository.findAll();

        for (InvestmentAccount acc : accounts) {
            snapshotService.createMonthlySnapshot(
                    acc.getCano(),
                    acc.getUserId(),
                    year,
                    month
            );
        }
    }
}
