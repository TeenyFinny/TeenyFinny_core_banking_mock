package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
import dev.syntax.domain.goal.dto.GoalAccountItemRes;
import dev.syntax.domain.goal.entity.GoalAccount;

public interface GoalAccountService {

    /**
     * 목표 계좌 생성
     * @param userId 요청한 사용자 ID
     * @param req 목표 계좌 생성 요청 DTO
     * @return 생성된 목표 계좌 정보 DTO
     */
    GoalAccountItemRes createGoalAccount(Long userId, GoalAccountCreateReq req);
}
