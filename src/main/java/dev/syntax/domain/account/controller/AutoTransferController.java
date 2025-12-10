package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.AllowanceUpdateAutoTransferReq;
import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.dto.GoalAutoTransferCreateReq;
import dev.syntax.domain.account.service.AutoTransferService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/banking/auto-transfer")
@RequiredArgsConstructor
public class AutoTransferController {

    private static final Logger log = LoggerFactory.getLogger(AutoTransferController.class);
    private final AutoTransferService autoTransferService;

    /**
     * 자동이체를 삭제합니다.
     *
     * <p>
     * 사용자가 등록한 자동이체(AutoTransfer)를 완전히 삭제(Hard Delete)하는 API입니다.
     * 자동이체는 상태 변경(CANCELLED) 없이 즉시 DB에서 제거되며,
     * 삭제가 성공하면 별도의 응답 본문 없이 HTTP 200 OK 상태만 반환합니다.
     * </p>
     *
     * <p><b>검증 로직:</b></p>
     * <ul>
     *     <li>1) {@code userId} 로 전달된 사용자 ID가 유효한지 확인</li>
     *     <li>2) {@code autoTransferId} 에 해당하는 자동이체가 존재하는지 확인</li>
     *     <li>3) 자동이체의 소유자가 요청한 사용자와 동일한지 권한 검증</li>
     * </ul>
     *
     * <p><b>예외 발생 시:</b></p>
     * <ul>
     *     <li>{@code USER_NOT_FOUND} - 사용자를 찾을 수 없음</li>
     *     <li>{@code AUTO_TRANSFER_NOT_FOUND} - 자동이체 정보 없음</li>
     *     <li>{@code AUTO_TRANSFER_FORBIDDEN} - 다른 사용자의 자동이체를 삭제하려는 경우</li>
     * </ul>
     *
     * @param userId         인증된 사용자 ID (현재 로그인 사용자)
     * @param autoTransferId 삭제할 자동이체의 고유 ID
     * @throws dev.syntax.global.exception.BusinessException 권한 없음, 사용자 없음, 자동이체 없음 등의 비즈니스 예외
     */
    @DeleteMapping("/{autoTransferId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAutoTransfer(
            @CurrentUserId Long userId,
            @PathVariable Long autoTransferId
    ) {
        log.info("[Core 자동 이체 삭제] userId: {}, autoTransferId: {}", userId, autoTransferId);
        autoTransferService.deleteAutoTransfer(userId, autoTransferId);
    }

    @PutMapping("/{autoTransferId}/pay-day")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAutoTransferDay(
            @CurrentUserId Long userId,
            @PathVariable Long autoTransferId,
            @RequestBody Integer payDay
    ) {
        autoTransferService.updateAutoTransferDay(userId, autoTransferId, payDay);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AutoTransferCreateRes createAutoTransfer(@CurrentUserId Long userId, @Valid @RequestBody AutoTransferCreateReq req) {
        return autoTransferService.createAutoTransfer(userId, req);
    }

    /**
     * 부모가 자녀의 목표 계좌로 자동이체를 등록하는 API.
     *
     * <p>
     * - 헤더의 {@code userId}: 부모 CoreUser ID<br>
     * - 바디의 {@code childCoreId}: 자녀 CoreUser ID
     * </p>
     */
    @PostMapping("/goal-by-user")
    @ResponseStatus(HttpStatus.CREATED)
    public AutoTransferCreateRes createChildGoalAutoTransfer(
            @CurrentUserId Long parentCoreId,
            @Valid @RequestBody GoalAutoTransferCreateReq req
    ) {
        return autoTransferService.createChildGoalAutoTransfer(parentCoreId, req);
    }

    /**
     * 자동이체 수정 API
     * <p>
     * 기존 자동이체의 정보를 수정합니다.
     * 출금/입금 계좌, 금액, 이체일, 메모를 변경할 수 있습니다.
     * </p>
     *
     * @param userId         X-Core-User-Id 헤더에서 추출된 사용자 ID
     * @param req            수정할 자동이체 정보
     * @param autoTransferId 수정할 자동이체 ID
     */
    @PutMapping("/{autoTransferId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateAutoTransfer(@CurrentUserId Long userId,
                                   @Valid @RequestBody AllowanceUpdateAutoTransferReq req,
                                   @PathVariable Long autoTransferId
    ) {
        autoTransferService.updateAutoTransfer(userId, req, autoTransferId);
    }
}
