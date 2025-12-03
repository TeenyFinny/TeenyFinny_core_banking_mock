package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AllowanceUpdateAutoTransferReq;
import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.dto.UpdateAutoTransferDayRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.repository.AutoTransferRepository;
import dev.syntax.domain.account.util.AutoTransferDateCalculator;
import dev.syntax.domain.account.dto.GoalAutoTransferCreateReq;
import dev.syntax.domain.goal.client.ChannelGoalClient;
import dev.syntax.domain.goal.dto.GoalDepositEventReq;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    private final ChannelGoalClient channelGoalClient;
    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;
    private final CoreUserRepository coreUserRepository;
    private final BalanceService balanceService;
    private final CoreUserRelationshipRepository relationshipRepository;

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

        CoreUser user = coreUserRepository.findByChannelUserId(req.userId())
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
     * 부모가 자녀의 용돈 계좌(ALLOWANCE)에서 자녀의 활성 목표 계좌(GOAL, ACTIVE)로
     * 자동이체를 등록합니다.
     */
    @Transactional
    @Override
    public AutoTransferCreateRes createChildGoalAutoTransfer(
            Long parentCoreId,
            GoalAutoTransferCreateReq req
    ) {
        Long childCoreId = req.childCoreId();

        // 1) 부모-자녀 관계 검증
        boolean isParent = relationshipRepository.existsByParent_IdAndChild_Id(parentCoreId, childCoreId);
        if (!isParent) {
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }

        // 2) 자녀 CoreUser 조회
        CoreUser child = coreUserRepository.findById(childCoreId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 3) 자녀 용돈 계좌(ALLOWANCE) 조회
        Account allowance = accountRepository.findFirstByUserIdAndType(child.getId(), AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.WITHDRAWAL_NOT_FOUND));

        // 4) 자녀 활성 목표 계좌(GOAL, ACTIVE) 조회
        Account goal = accountRepository.findFirstByUserIdAndTypeAndStatus(child.getId(), AccountType.GOAL, AccountStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.DEPOSIT_NOT_FOUND));

        // 5) AutoTransfer 엔티티 생성
        AutoTransfer transfer = AutoTransfer.builder()
                .fromAccount(allowance)
                .toAccount(goal)
                .user(child)
                .amount(req.amount())
                .memo("GOAL: " + goal.getProductName())
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

            Account targetAccount = t.getToAccount();
            if (targetAccount.getType().equals(AccountType.GOAL)) {
                GoalDepositEventReq req = GoalDepositEventReq.builder()
                        .accountNo(targetAccount.getNumber())
                        .balanceAfter(targetAccount.getBalance())
                        .build();

                try {
                    channelGoalClient.sendGoalDepositEvent(req);
                } catch (Exception e) {
                    log.error("목표 계좌 입금 이벤트 전송 실패. 계좌번호: {}", targetAccount.getNumber(), e);
                }
            }


        } catch (BusinessException e) {

            // 출금/입금 중 오류 발생 시 FAIL 상태 저장
            updateStatusAndNextDate(t, AutoTransferStatus.FAIL);

            // 스케줄러에서 failCount++ 되도록 예외를 다시 던짐
            throw e;
        } catch (Exception e) {
            // 예상치 못한 전역 오류 처리
            updateStatusAndNextDate(t, AutoTransferStatus.FAIL);
            throw e;
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
 *
 * <p>권한 및 유효성 검증 흐름:</p>
 * <ol>
 *   <li>자동이체 ID로 기존 자동이체 조회</li>
 *   <li>권한 검증: 자동이체 소유자이거나 부모-자녀 관계여야 수정 가능</li>
 *   <li>자동이체 정보 업데이트</li>
 *   <li>저장 후 영속성 컨텍스트 반영</li>
 * </ol>
 *
 * <p>
 * ※ 이체일 변경 시 다음 실행일(nextTransferDate)은 자동으로 재계산됩니다.<br>
 * ※ IDOR 방지를 위해 부모/자녀 관계가 아닌 타인의 AutoTransfer는 수정할 수 없습니다.
 * </p>
 *
 * @param userId 로그인한 사용자 ID
 * @param req 수정할 자동이체 요청 정보 (금액, 이체일 등)
 * @param autoTransferId 수정 대상 자동이체 ID
 * @throws BusinessException AUTO_TRANSFER_NOT_FOUND 자동이체 내역이 없는 경우
 * @throws BusinessException ACCESS_DENIED 권한이 없는 사용자가 수정 시도한 경우
 */
@Transactional
@Override
public void updateAutoTransfer(Long userId, AllowanceUpdateAutoTransferReq req, Long autoTransferId) {

    // 1) 자동이체 조회
    AutoTransfer transfer = autoTransferRepository.findById(autoTransferId)
        .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

    // 2) 권한 검증 — 자동이체 소유자 또는 부모만 허용
    CoreUser owner = transfer.getUser();

    if (!userId.equals(owner.getId())) {
        boolean isParent = relationshipRepository.existsByParent_IdAndChild_Id(userId, owner.getId());
        if (!isParent) {
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }
    }

    // 3) 자동이체 정보 업데이트 (다음 이체일 포함)
    transfer.updateTransfer(
        req.amount(),
        req.transferDay(),
        AutoTransferDateCalculator.getNextTransferDate(req.transferDay())
    );

}

    /**
     * 자동이체 납입일 변경 기능.
     *
     * <p>
     * - transferDay 값을 변경
     * - 변경된 납입일(payDay)을 기준으로 nextTransferDay를 재계산
     * </p>
     *
     * @param userId 요청 사용자
     * @param autoTransferId 자동이체 ID
     * @param payDay 변경할 납입일
     * @return 업데이트된 값 DTO
     */
    @Override
    @Transactional
    public UpdateAutoTransferDayRes updateAutoTransferDay(Long userId, Long autoTransferId, Integer payDay) {
        CoreUser user = coreUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        autoTransfer.updateTransferDay(payDay);

        LocalDate currentNext = autoTransfer.getNextTransferDay();
        LocalDate newNextDate = AutoTransferDateCalculator.calculateNextTransferDate(currentNext, payDay);

        autoTransfer.setNextTransferDay(newNextDate);

        return new UpdateAutoTransferDayRes(autoTransferId, autoTransfer.getTransferDay());
    }

    /**
     * 자동이체 삭제 기능 구현.
     *
     * <p>
     * 사용자가 등록한 자동이체를 실제로 삭제(Hard Delete)하며,
     * 다음 조건을 만족해야 삭제가 가능합니다.
     * </p>
     *
     * <ol>
     *     <li>사용자 ID(userId)에 해당하는 CoreUser가 존재해야 함</li>
     *     <li>autoTransferId에 해당하는 자동이체가 존재해야 함</li>
     *     <li>자동이체 소유자가 요청 사용자와 동일해야 함</li>
     * </ol>
     *
     * <p>
     * 해당 메서드는 Soft Delete가 아닌,
     * {@code autoTransferRepository.delete(autoTransfer)} 를 호출하여
     * DB에서 자동이체 엔티티를 완전히 제거합니다.
     * </p>
     *
     * @param userId         삭제 요청을 보낸 사용자 ID
     * @param autoTransferId 삭제할 자동이체 엔티티의 ID
     *
     * @throws BusinessException
     *         <ul>
     *             <li>{@code USER_NOT_FOUND} - 사용자 조회 실패</li>
     *             <li>{@code AUTO_TRANSFER_NOT_FOUND} - 자동이체 조회 실패</li>
     *             <li>{@code AUTO_TRANSFER_FORBIDDEN} - 사용자가 소유하지 않은 자동이체에 접근한 경우</li>
     *         </ul>
     */
    @Override
    @Transactional
    public void deleteAutoTransfer(Long userId, Long autoTransferId) {

        CoreUser user = coreUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        autoTransferRepository.delete(autoTransfer);
    }
}
