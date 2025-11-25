package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.AccountStatusUpdateReq;
import dev.syntax.domain.account.dto.AccountStatusUpdateRes;
import dev.syntax.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core/account")
public class AccountController {

    private final AccountService accountService;

    @PatchMapping("/{number}/status")
    public AccountStatusUpdateRes updateAccountStatus(
            @PathVariable String number,
            @RequestBody AccountStatusUpdateReq req
    ) {
        return accountService.updateStatus(number, req.status());
    }
}
