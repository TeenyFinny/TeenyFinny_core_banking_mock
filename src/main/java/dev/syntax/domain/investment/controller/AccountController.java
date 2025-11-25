package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.AccountItemRes;
import dev.syntax.domain.investment.entity.InvestmentAccount;
import dev.syntax.domain.investment.service.InvestmentAccountService;
import dev.syntax.global.auth.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/banking/account")
@RequiredArgsConstructor
public class AccountController {

    private final InvestmentAccountService investmentAccountService;

    /**
     * 투자계좌 생성 API
     * POST /core/banking/account/investment
     */
    @PostMapping("/investment")
    public ResponseEntity<AccountItemRes> createInvestmentAccount(@CurrentUserId Long userId) {
        // 초기 예수금 0으로 세팅 (필요 시 프론트에서 받을 수도 있음)
        InvestmentAccount account = investmentAccountService.createInvestmentAccount(userId, 0L);

        AccountItemRes res = AccountItemRes.builder()
                .accountNumber(account.getCano())
                .userId(account.getUserId())
                .balance(account.getDepositAmount())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }
}
