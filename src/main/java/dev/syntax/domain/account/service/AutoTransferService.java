package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AllowanceUpdateAutoTransferReq;
import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.dto.GoalAutoTransferCreateReq;
import dev.syntax.domain.account.dto.UpdateAutoTransferDayRes;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.util.AutoTransferDateCalculator;
import dev.syntax.global.exception.BusinessException;

import java.time.LocalDate;
import java.util.List;

/**
 * 자동이체 관리 서비스
 * <p>
 * 자동이체 등록, 실행, 조회 기능을 제공합니다.
 * 매달 지정된 날짜에 자동으로 계좌간 이체가 실행됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 * @see AutoTransfer
 * @see AutoTransferDateCalculator
 */
public interface AutoTransferService {

    /**
     * 자동이체를 등록합니다.
     * <p>
     * 매달 지정된 날짜에 자동으로 출금 계좌에서 입금 계좌로 이체하는 자동이체를 생성합니다.
     * 다음 실행일은 AutoTransferDateCalculator를 통해 자동으로 계산됩니다.
     * </p>
     *
     * @param fromAccountId 출금 계좌 ID
     * @param toAccountId   입금 계좌 ID
     * @param user          자동이체를 등록하는 사용자
     * @param amount        이체 금액
     * @param transferDay   매달 실행될 날짜 (1~31, 31일 이상은 마지막 날로 처리)
     * @param memo          자동이체 메모
     * @return 생성된 AutoTransfer 엔티티
     * @throws BusinessException 출금 또는 입금 계좌를 찾을 수 없는 경우
     */
    AutoTransferCreateRes createAutoTransfer(
            Long userId,
            AutoTransferCreateReq req
    );

    /**
     * 부모가 자녀의 목표 계좌로 자동이체를 등록합니다.
     *
     * <p>
     * - parentCoreId: 현재 로그인한 부모의 CoreUser ID (헤더에서 주입)<br>
     * - childCoreId: 자동이체 대상이 되는 자녀의 CoreUser ID (body에서 전달)
     * </p>
     */
    AutoTransferCreateRes createChildGoalAutoTransfer(
            Long parentCoreId,
            GoalAutoTransferCreateReq req
    );

    /**
     * 자동이체를 실행합니다.
     * <p>
     * 1. 출금 계좌에서 금액을 출금 (AUTO_WITHDRAW 코드)
     * 2. 입금 계좌로 금액을 입금 (AUTO_DEPOSIT 코드)
     * 3. 성공/실패 상태 업데이트
     * 4. 다음 실행일 계산 및 업데이트
     * </p>
     *
     * @param t 실행할 AutoTransfer 엔티티
     */
    void execute(AutoTransfer t);

    /**
     * 오늘 실행해야 하는 모든 자동이체를 조회합니다.
     * <p>
     * nextTransferDay가 오늘 날짜인 모든 자동이체를 반환합니다.
     * </p>
     *
     * @return 오늘 실행될 자동이체 목록
     */
    List<AutoTransfer> findTransfersByDate(LocalDate date);

    /**
     * 자동이체 정보를 수정합니다.
     * <p>
     * 기존 자동이체의 출금/입금 계좌, 금액, 이체일, 메모를 수정합니다.
     * 이체일이 변경되면 다음 실행일도 자동으로 재계산됩니다.
     * </p>
     *
     * @param userId         자동이체를 수정하는 사용자 ID
     * @param req            수정할 자동이체 정보 (출금/입금 계좌, 금액, 이체일, 메모)
     * @param autoTransferId 수정할 자동이체 ID
     * @throws BusinessException 자동이체, 계좌, 또는 사용자를 찾을 수 없는 경우
     */
    void updateAutoTransfer(Long userId, AllowanceUpdateAutoTransferReq req, Long autoTransferId);

    UpdateAutoTransferDayRes updateAutoTransferDay(Long userId, Long autoTransferId, Integer payDay);

    /**
     * 자동이체를 삭제합니다.
     *
     * <p>
     * 사용자가 등록한 자동이체(AutoTransfer)를 완전히 삭제(Hard Delete)하는 기능입니다.
     * 다음 조건을 모두 만족하는 경우에만 삭제가 가능합니다.
     * </p>
     *
     * <ul>
     *     <li>1) 요청한 사용자(userId)가 존재해야 합니다.</li>
     *     <li>2) autoTransferId에 해당하는 자동이체가 존재해야 합니다.</li>
     *     <li>3) 자동이체의 소유자(ID)가 요청한 사용자와 일치해야 합니다.</li>
     * </ul>
     *
     * <p>
     * 삭제 방식은 Soft Delete(상태 변경)가 아닌
     * JPA Repository의 {@code delete()} 메서드를 사용한 실제 삭제(Hard Delete)를 수행합니다.
     * 자동이체 기록을 보존할 필요가 없는 비즈니스 요구에 적합합니다.
     * </p>
     *
     * @param userId           자동이체 삭제 요청을 보낸 사용자 ID
     * @param autoTransferId   삭제할 자동이체의 고유 ID
     *
     * @throws BusinessException
     *         <ul>
     *             <li>{@code USER_NOT_FOUND} - 사용자 조회 실패</li>
     *             <li>{@code AUTO_TRANSFER_NOT_FOUND} - 자동이체 조회 실패</li>
     *             <li>{@code AUTO_TRANSFER_FORBIDDEN} - 사용자가 소유하지 않은 자동이체에 접근한 경우</li>
     *         </ul>
     *
     * @author
     *      TeenyFinny Core Banking Team
     * @see AutoTransfer
     * @see dev.syntax.domain.account.repository.AutoTransferRepository
     */
    void deleteAutoTransfer(Long userId, Long autoTransferId);
}

