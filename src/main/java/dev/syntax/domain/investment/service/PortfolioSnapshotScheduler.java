package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.repository.InvestAccountRepository;
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

    private final InvestAccountRepository accountRepository;
    private final PortfolioSnapshotService snapshotService;

    @Scheduled(cron = "0 5 0 1 * *") // 매월 1일 00:05
    public void runMonthlySnapshot() {

        LocalDate now = LocalDate.now().minusMonths(1);
        int year = now.getYear();
        int month = now.getMonthValue();

        List<InvestAccount> accounts = accountRepository.findAll();

        for (InvestAccount acc : accounts) {
            snapshotService.createMonthlySnapshot(
                    acc.getCano(),
                    acc.getUserId(),
                    year,
                    month
            );
        }
    }
}
