package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalAccountItemRes;

public interface GoalAccountService {

    /**
     * 목표계좌 생성
     *
     * @param userId 사용자 ID
     * @param goalName 목표 이름
     * @return 생성된 목표계좌 정보
     */
    GoalAccountItemRes createGoalAccount(Long userId, String goalName);
}