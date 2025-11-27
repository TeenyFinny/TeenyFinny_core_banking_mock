package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.repository.AutoTransferRepository;
import dev.syntax.domain.account.util.AutoTransferDateCalculator;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoTransferServiceImpl implements AutoTransferService {

    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;
    private final CoreUserRepository coreUserRepository;
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
    public AutoTransferCreateRes createAutoTransfer(
            Long userId,
            AutoTransferCreateReq req
    ) {
        Account from = accountRepository.findById(req.fromAccountId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.WITHDRAWAL_NOT_FOUND)); // 출금 계좌 없음
        Account to = accountRepository.findById(req.toAccountId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.DEPOSIT_NOT_FOUND)); // 입금 계좌 없음

        CoreUser user = coreUserRepository.findById(req.userId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND)); // 사용자 없음
        AutoTransfer transfer = AutoTransfer.builder()
                .fromAccount(from)
                .toAccount(to)
                .user(user)
                .amount(req.amount())
                .memo(req.memo())
                .transferDay(req.transferDay())
                .nextTransferDay(AutoTransferDateCalculator.getNextTransferDate(req.transferDay()))
                .status(AutoTransferStatus.PROCESSING)
                .build();

        autoTransferRepository.save(transfer);

        return new AutoTransferCreateRes(transfer.getId());
    }

    /**
     * 자동이체를 실행합니다.
     *
     * <p>트랜잭션 처리 특징:</p>
     * <ul>
     *   <li>출금/입금 중 예외 발생 시 execute() 전체는 롤백됨</li>
     *   <li>그러나 상태/다음 실행일 업데이트는
     *       {@link #updateStatusAndNextDate(AutoTransfer, AutoTransferStatus)}
     *       의 REQUIRES_NEW 트랜잭션으로 분리되어 항상 DB에 반영됨</li>
     *   <li>이로 인해 실패한 자동이체가 반복 실행되는 문제를 방지함</li>
     * </ul>
     */
    @Transactional
    @Override
    public void execute(AutoTransfer t) {

        try {
            // 1) 출금 (AUTO_WITHDRAW) - 자동이체 출금 거래 기록
            balanceService.withdraw(
                    t.getFromAccount().getId(),
                    t.getUser(),
                    t.getAmount(),
                    "자동이체 출금",
                    TransactionCategory.TRANSFER,
                    null, // 자동이체는 카드 내역 구분에 포함되지 않음
                    TransactionCode.AUTO_WITHDRAW
            );

            // 2) 입금 (AUTO_DEPOSIT)- 자동이체 입금 거래 기록
            balanceService.deposit(
                    t.getToAccount().getId(),
                    t.getUser(),
                    t.getAmount(),
                    "자동이체 입금",
                    TransactionCategory.TRANSFER,
                    null,
                    TransactionCode.AUTO_DEPOSIT
            );

            // 3) 실행 성공 처리
            // (상태 + 다음 실행일) → REQUIRES_NEW 트랜잭션으로 별도 반영
            updateStatusAndNextDate(t, AutoTransferStatus.SUCCESS);

        } catch (RuntimeException e) {

            // 출금/입금 중 오류 발생 시 FAIL 상태 저장
            updateStatusAndNextDate(t, AutoTransferStatus.FAIL);
        }
    }

    /**
     * 자동이체 상태 및 다음 실행일을 갱신하는 메서드.
     *
     * <p>Propagation.REQUIRES_NEW로 설정되어 있어,
     * execute() 트랜잭션이 실패하더라도 이 로직은 별도의 트랜잭션으로 커밋됩니다.
     * 자동이체 실패가 반복 실행되는 문제를 방지하는 핵심 포인트입니다.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateStatusAndNextDate(AutoTransfer t, AutoTransferStatus status) {

        // 상태 변경 (SUCCESS / FAIL)
        t.setStatus(status);

        // 다음 실행일 갱신
        t.setNextTransferDay(
                AutoTransferDateCalculator.getNextTransferDate(t.getTransferDay())
        );

        // 별도의 트랜잭션으로 강제 커밋됨
        autoTransferRepository.save(t);
    }

    /**
     * 오늘 실행해야 하는 모든 자동이체를 조회합니다.
     * <p>
     * nextTransferDay가 오늘인 모든 자동이체를 반환합니다.
     * </p>
     */
    @Override
    public List<AutoTransfer> findTransfersByDate(LocalDate date) {
        return autoTransferRepository.findByNextTransferDay(date);
    }

    /**
     * 자동이체 정보를 수정합니다.
     * <p>
     * 1. 자동이체 ID로 기존 자동이체 조회
     * 2. 사용자 및 출금/입금 계좌 검증
     * 3. 새로운 정보로 AutoTransfer 엔티티 생성
     * 4. 기존 엔티티에 새 정보 업데이트
     * 5. 변경사항 저장
     * </p>
     * <p>
     * 이체일이 변경되면 다음 실행일도 자동으로 재계산됩니다.
     * </p>
     */
    @Transactional
    @Override
    public void updateAutoTransfer(Long userId, AutoTransferCreateReq req, Long autoTransferId) {
        // 먼저 자동이체 아이디를 조회
        // req에 있는 정보로 업데이트
        // 1) 자동이체 조회
        AutoTransfer transfer = autoTransferRepository.findById(autoTransferId)
            .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        // 3) 자녀 아이디로 사용자 조회
        CoreUser user = coreUserRepository.findById(req.userId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND)); // 사용자 없음

        // 3) 출금 계좌, 입금 계좌 확인
        Account from = accountRepository.findById(req.fromAccountId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.WITHDRAWAL_NOT_FOUND)); // 출금 계좌 없음
        Account to = accountRepository.findById(req.toAccountId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.DEPOSIT_NOT_FOUND)); // 입금 계좌 없음

        // 4) AutoTransfer 엔티티 생성
        AutoTransfer newTransfer = AutoTransfer.builder()
                .fromAccount(from)
                .toAccount(to)
                .amount(req.amount())
                .memo(req.memo())
                .transferDay(req.transferDay())
                .nextTransferDay(AutoTransferDateCalculator.getNextTransferDate(req.transferDay()))
                .build();

        // 5) AutoTransfer 엔티티 업데이트
        transfer.updateTransfer(newTransfer);

        // 6) AutoTransfer 엔티티 저장
        autoTransferRepository.save(transfer);
    }
}
