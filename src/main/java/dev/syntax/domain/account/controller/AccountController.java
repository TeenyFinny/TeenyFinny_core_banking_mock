package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.user.service.InitService;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final InitService initService;

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
    public ResponseEntity<BaseResponse<?>> createDepositAccount(@Valid @RequestBody DepositAccountReq req) {
        Account account = accountService.createChildDepositAccount(req);
        AccountItemRes response = AccountItemRes.from(account);
        return ApiResponseUtil.success(SuccessCode.OK, response);
    }
}
