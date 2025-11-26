package dev.syntax.domain.goal.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

    private final CoreUserRepository userRepository;
    private final CoreUserRelationshipRepository relationshipRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createGoalAccount(Long userId, GoalAccountCreateReq req) {
        if (!relationshipRepository.existsByParent_IdAndChild_Id(userId, req.childCoreId())){
            log.warn("[GOAL] 가족 관계 생성 누락");
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }
        // 1. 사용자 조회
        CoreUser user = userRepository.findById(req.childCoreId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 2. 목표 계좌 생성
        Account account = Account.builder()
                .user(user)
                .number(AccountNumberGenerator.generate())
                .productName(req.name())
                .interestRate(new BigDecimal("0.001"))
                .balance(BigDecimal.ZERO)
                .type(AccountType.GOAL)
                .build();

        // 3. 저장 후 엔티티 반환
        accountRepository.save(account);

        return account;
    }
}
