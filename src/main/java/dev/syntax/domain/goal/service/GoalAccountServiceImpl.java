package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
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
    public GoalAccountItemRes createGoalAccount(Long userId, GoalAccountCreateReq req) {

        // 1. 사용자 조회
        CoreUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 2. 목표 계좌 생성
        GoalAccount account = GoalAccount.builder()
                .user(user)
                .goalName(req.name())
                .balance(BigDecimal.ZERO)
                .accountNumber(UUID.randomUUID().toString())
                .build();

        // 3. 저장 후 엔티티 반환
        goalAccountRepository.save(account);

        return GoalAccountItemRes.from(account);
    }
}
