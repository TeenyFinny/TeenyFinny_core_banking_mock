package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.repository.AutoTransferRepository;
import dev.syntax.domain.account.util.AutoTransferDateCalculator;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * {@link AutoTransferService}의 구현체
 * <p>
 * 자동이체 등록, 실행, 조회 기능을 처리하며,
 * BalanceService를 통해 실제 계좌 잔액 변경을 수행합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoTransferServiceImpl implements AutoTransferService {

    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;
    private final BalanceService balanceService;

    /**
     * 자동이체를 등록합니다.
     * <p>
     * 1. 출금/입금 계좌 조회 및 검증
     * 2. AutoTransferDateCalculator로 다음 실행일 계산
     * 3. AutoTransfer 엔티티 생성 및 저장
     * </p>
     */
    @Transactional
    @Override
    public AutoTransfer create(
            Long fromAccountId,
            Long toAccountId,
            CoreUser user,
            BigDecimal amount,
            int transferDay,
            String memo
    ) {
        Account from = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.WITHDRAWAL_NOT_FOUND)); // 출금 계좌 없음
        Account to = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.DEPOSIT_NOT_FOUND)); // 입금 계좌 없음

        AutoTransfer transfer = AutoTransfer.builder()
                .fromAccount(from)
                .toAccount(to)
                .user(user)
                .amount(amount)
                .memo(memo)
                .transferDay(transferDay)
                .nextTransferDay(AutoTransferDateCalculator.getNextTransferDate(transferDay))
                .status(AutoTransferStatus.PROCESSING)
                .build();

        return autoTransferRepository.save(transfer);
    }

    /**
     * 자동이체를 실행합니다.
     * <p>
     * try-catch로 감싸져 실패 시에도 다음 실행일은 갱신됩니다.
     * 1. BalanceService.withdraw() - AUTO_WITHDRAW
     * 2. BalanceService.deposit() - AUTO_DEPOSIT
     * 3. 성공 시 SUCCESS, 실패 시 FAIL 상태로 변경
     * 4. 다음 실행일 계산 및 업데이트
     * </p>
     */
    @Transactional
    @Override
    public void execute(AutoTransfer t) {

        try {
            // 1) 출금 (AUTO_WITHDRAW)
            balanceService.withdraw(
                    t.getFromAccount(),
                    t.getUser(),
                    t.getAmount(),
                    "자동이체 출금",
                    TransactionCategory.TRANSFER,
                    null, // 자동이체는 카드 내역 구분에 포함되지 않음
                    TransactionCode.AUTO_WITHDRAW
            );

            // 2) 입금 (AUTO_DEPOSIT)
            balanceService.deposit(
                    t.getToAccount(),
                    t.getUser(),
                    t.getAmount(),
                    "자동이체 입금",
                    TransactionCategory.TRANSFER,
                    null,
                    TransactionCode.AUTO_DEPOSIT
            );

            // 3) SUCCESS 상태로 변경
            t = autoTransferRepository.findById(t.getId()).orElse(t);
            t.setStatus(AutoTransferStatus.SUCCESS);

        } catch (Exception e) {

            // 실패 처리
            t = autoTransferRepository.findById(t.getId()).orElse(t);
            t.setStatus(AutoTransferStatus.FAIL);
        }

        // 4) 다음 실행일 갱신
        t.setNextTransferDay(
                AutoTransferDateCalculator.getNextTransferDate(t.getTransferDay())
        );

        autoTransferRepository.save(t);
    }

    /**
     * 오늘 실행해야 하는 모든 자동이체를 조회합니다.
     * <p>
     * nextTransferDay가 오늘인 모든 자동이체를 반환합니다.
     * </p>
     */
    @Override
    public List<AutoTransfer> findTodayTransfers() {
        LocalDate today = LocalDate.now();
        return autoTransferRepository.findByNextTransferDay(today);
    }
}
