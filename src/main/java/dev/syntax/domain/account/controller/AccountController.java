package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.AccountStatusUpdateReq;
import dev.syntax.domain.account.dto.AccountStatusUpdateRes;
import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.account.service.AutoTransferService;
import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.dto.UserAccountListRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.global.auth.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 계좌 관련 컨트롤러
 * <p>
 * 계좌 생성 등 계좌 관련 API를 처리합니다.
 * </p>
 */
@RestController
@RequestMapping("/core/banking/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AutoTransferService autoTransferService;

    @PatchMapping("/{number}/status")
    public AccountStatusUpdateRes updateAccountStatus(
            @PathVariable String number,
            @RequestBody AccountStatusUpdateReq req
    ) {
        return accountService.updateStatus(number, req.status());
    }
    
      /**
     * 자녀 계좌 생성 API
     * <p>
     * 자녀의 입출금 통장 계좌를 생성하고 부모-자녀 간 가족 관계를 매핑합니다.
     * </p>
     * <ul>
     *   <li>부모-자녀 간 가족 관계 생성</li>
     *   <li>자녀의 입출금 통장 계좌 생성</li>
     * </ul>
     *
     * @param req 계좌 생성 요청 정보 (부모 ID, 자녀 ID, 계좌 타입 포함)
     * @return 생성된 계좌 정보
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountItemRes createChildAllowanceAccount(@CurrentUserId Long userId, @Valid @RequestBody DepositAccountReq req) {
        Account account = accountService.createChildAllowanceAccount(userId, req);
        return AccountItemRes.from(account);
    }

    /**
     * 사용자 전체 계좌 조회 API
     * <p>
     * 특정 사용자의 전체 계좌를 조회합니다.
     * 부모일 경우 자녀의 계좌까지 포함하여 반환합니다.
     * </p>
     * <p>
     * `@CurrentUserId` 어노테이션을 통해 SecurityContext에서 자동으로 userId를 주입받습니다.
     * </p>
     *
     * @param userId X-Core-User-Id 헤더에서 추출된 사용자 ID
     * @return 계좌 목록
     */
    @GetMapping
    public UserAccountListRes getAccounts(@CurrentUserId Long userId) {
        return accountService.getUserAccounts(userId);
    }

    @PostMapping("/auto-transfer/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AutoTransferCreateRes createAutoTransfer(@CurrentUserId Long userId, @Valid @RequestBody AutoTransferCreateReq req) {
        return autoTransferService.createAutoTransfer(userId, req);
    }
}
