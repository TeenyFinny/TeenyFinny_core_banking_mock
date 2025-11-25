package dev.syntax.domain.goal.controller;

import dev.syntax.domain.goal.dto.GoalAccountItemRes;
import dev.syntax.domain.goal.service.GoalAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/core/banking/goal/account")
@RequiredArgsConstructor
public class GoalAccountController {

    private final GoalAccountService goalAccountService;

    /**
     * 목표계좌 생성 API
     * POST /core/banking/goal/account
     */
    @PostMapping
    public ResponseEntity<GoalAccountItemRes> createGoalAccount(
            @RequestParam Long userId,
            @RequestParam String name) {

        GoalAccountItemRes res = goalAccountService.createGoalAccount(userId, name);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }
}
