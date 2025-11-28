package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.AccountItemRes;
import dev.syntax.domain.investment.entity.InvestAccount;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/banking/account")
@RequiredArgsConstructor
public class InvestAccountController {

    private final InvestAccountService investAccountService;

    /**
     * 투자계좌 생성 API
     * POST /core/banking/account/investment
     */
    @PostMapping("/investment")
    public AccountItemRes createInvestmentAccount(@CurrentUserId Long userId) {
        // 초기 예수금 0으로 세팅 (필요 시 프론트에서 받을 수도 있음)
        InvestAccount account = investAccountService.createInvestmentAccount(userId, 0L);

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
