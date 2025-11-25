package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
import dev.syntax.domain.goal.dto.GoalAccountItemRes;
import dev.syntax.domain.goal.entity.GoalAccount;
import dev.syntax.domain.goal.repository.GoalAccountRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GoalAccountServiceImplTest {

    private final CoreUserRepository userRepository = mock(CoreUserRepository.class);
    private final GoalAccountRepository goalAccountRepository = mock(GoalAccountRepository.class);

    private final GoalAccountServiceImpl service =
            new GoalAccountServiceImpl(userRepository, goalAccountRepository);


    @Test
    @DisplayName("정상적으로 목표 계좌가 생성된다")
    void createGoalAccount_success() {
        // given
        Long userId = 1L;
        String goalName = "닌텐도 스위치 사기";
        GoalAccountCreateReq req = new GoalAccountCreateReq(goalName);

        CoreUser user = CoreUser.builder()
                .id(userId)
                .name("홍길동")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(goalAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        GoalAccountItemRes res = service.createGoalAccount(userId, req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getUserId()).isEqualTo(userId);
        assertThat(res.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(res.getAccountNumber()).isNotBlank();

        // 저장된 엔티티 검증
        ArgumentCaptor<GoalAccount> captor = ArgumentCaptor.forClass(GoalAccount.class);
        verify(goalAccountRepository).save(captor.capture());

        GoalAccount saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getGoalName()).isEqualTo(goalName);
        assertThat(saved.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(saved.getAccountNumber()).isNotNull();
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외가 발생한다")
    void createGoalAccount_userNotFound() {
        // given
        Long userId = 99L;
        String goalName = "저축";
        GoalAccountCreateReq req = new GoalAccountCreateReq(goalName);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.createGoalAccount(userId,req)
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorBaseCode.USER_NOT_FOUND);
        verify(goalAccountRepository, never()).save(any());
    }
}
