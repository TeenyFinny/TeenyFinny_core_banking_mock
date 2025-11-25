package dev.syntax.domain.goal.service;


import dev.syntax.domain.goal.dto.GoalAccountItemRes;
import dev.syntax.domain.goal.entity.GoalAccount;
import dev.syntax.domain.goal.repository.GoalAccountRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

    private final CoreUserRepository userRepository;
    private final GoalAccountRepository goalAccountRepository;

    @Override
    @Transactional
    public GoalAccountItemRes createGoalAccount(Long userId, String goalName) {
        // 1. 사용자 조회
        CoreUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 2. 계좌 생성
        GoalAccount account = GoalAccount.builder()
                .user(user)
                .goalName(goalName)
                .balance(BigDecimal.ZERO)
                .accountNumber(UUID.randomUUID().toString()) // UUID 생성
                .build();

        goalAccountRepository.save(account);

        // 3. DTO 반환
        return GoalAccountItemRes.builder()
                .accountNumber(account.getAccountNumber())
                .userId(user.getId())
                .balance(account.getBalance())
                .build();
    }
}