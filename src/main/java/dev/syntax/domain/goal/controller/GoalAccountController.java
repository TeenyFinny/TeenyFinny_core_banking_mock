package dev.syntax.domain.goal.controller;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
import dev.syntax.domain.goal.service.GoalAccountService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 목표 계좌 관련 컨트롤러
 * <p>
 * 코어 뱅킹 서비스에서 목표 계좌를 생성하는 API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/core/banking/goal/account")
@RequiredArgsConstructor
public class GoalAccountController {

    private final GoalAccountService goalAccountService;

    /**
     * 목표 계좌 생성 API
     * <p>
     * POST /core/banking/goal/account
     * <p>
     * 인증된 사용자 ID와 목표 계좌 생성 요청 정보를 받아서
     * 목표 계좌를 생성하고, 생성된 계좌 정보를 반환합니다.
     *
     * @param userId 인증된 사용자 ID (CurrentUserId 어노테이션을 통해 주입)
     * @param req    목표 계좌 생성 요청 정보 (목표 이름)
     * @return 생성된 목표 계좌 정보
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountItemRes createGoalAccount(
            @CurrentUserId Long userId,
            @Valid @RequestBody GoalAccountCreateReq req
    ) {
        Account account =  goalAccountService.createGoalAccount(userId, req);
        return AccountItemRes.from(account);
    }
}
