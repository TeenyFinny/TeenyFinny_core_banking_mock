package dev.syntax.domain.investment.controller;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.investment.dto.AccountItemRes;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.auth.annotation.CurrentUserId;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/banking/account")
@RequiredArgsConstructor
public class InvestAccountController {

    private final InvestAccountService investAccountService;
    private final AccountService accountService;
    private final CoreUserRepository coreUserRepository;
    /**
     * 투자계좌 생성 API
     * POST /core/banking/account/investment
     */
    @PostMapping("/investment")
    public AccountItemRes createInvestmentAccount(@RequestParam Long userId) {
        // 자녀 CoreUser 조회
        CoreUser child = coreUserRepository.findByChannelUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.CHILD_USER_NOT_FOUND));
        // core_account 테이블에 추가
        accountService.createInvestAccount(child);


        // 초기 예수금 0으로 세팅 (필요 시 프론트에서 받을 수도 있음)
        InvestAccount account = investAccountService.createInvestmentAccount(child.getId(), 0L);

        return AccountItemRes.builder()
                .accountNumber(account.getCano())
                .userId(account.getUserId())
                .balance(account.getDepositAmount())
                .build();
    }

    /**
     * 계좌 존재 여부 확인 API
     * GET /core/banking/account/check
     */
    @GetMapping("/check")
    public boolean checkAccount(@CurrentUserId Long userId) {
        return investAccountService.checkAccount(userId);
    }
}
