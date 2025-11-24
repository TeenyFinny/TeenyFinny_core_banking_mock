package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.util.AutoTransferDateCalculator;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;

import java.math.BigDecimal;
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
    AutoTransfer create(
            Long fromAccountId,
            Long toAccountId,
            CoreUser user,
            BigDecimal amount,
            int transferDay,
            String memo
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
}
