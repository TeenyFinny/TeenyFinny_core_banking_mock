package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.service.AutoTransferService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/banking/auto-transfer")
@RequiredArgsConstructor
public class AutoTransferController {

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
     *
     * @throws dev.syntax.global.exception.BusinessException
     *      권한 없음, 사용자 없음, 자동이체 없음 등의 비즈니스 예외
     */
    @DeleteMapping("/{autoTransferId}")
    public void deleteAutoTransfer(
            @CurrentUserId Long userId,
            @PathVariable Long autoTransferId
    ) {
        autoTransferService.deleteAutoTransfer(userId, autoTransferId);
    }

}
