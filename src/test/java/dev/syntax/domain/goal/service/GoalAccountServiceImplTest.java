package dev.syntax.domain.goal.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.dto.GoalAccountCreateReq;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GoalAccountServiceImplTest {

    @InjectMocks
    private GoalAccountServiceImpl goalAccountService;

    @Mock
    private CoreUserRepository userRepository;

    @Mock
    private CoreUserRelationshipRepository relationshipRepository;

    @Mock
    private AccountRepository accountRepository;

    // ======================================================================================
    // SUCCESS: GOAL 계좌 생성, 상태 = SUSPENDED
    // ======================================================================================
    @Test
    @DisplayName("SUCCESS - 가족 관계가 존재하면 GOAL 계좌 생성되고 상태=SUSPENDED 이어야 한다")
    void createGoalAccount_success() {
        // given
        Long parentId = 1L;
        Long childId = 2L;

        GoalAccountCreateReq req = new GoalAccountCreateReq(childId, "저축 목표");

        // 가족 관계 존재
        given(relationshipRepository.existsByParent_IdAndChild_Id(parentId, childId))
                .willReturn(true);

        // 자녀 정보 조회
        CoreUser child = CoreUser.builder().name("자녀").build();
        ReflectionTestUtils.setField(child, "id", childId);

        given(userRepository.findById(childId)).willReturn(Optional.of(child));

        // 저장되는 계좌 Mock
        Account savedAccount = Account.builder()
                .user(child)
                .balance(BigDecimal.ZERO)
                .productName("저축 목표")
                .type(AccountType.GOAL)
                .status(AccountStatus.SUSPENDED)   // 요구사항: 목표 계좌는 생성 시 SUSPENDED
                .build();

        ReflectionTestUtils.setField(savedAccount, "id", 100L);

        given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

        // when
        Account result = goalAccountService.createGoalAccount(parentId, req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(AccountType.GOAL);
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getProductName()).isEqualTo("저축 목표");
        assertThat(result.getStatus()).isEqualTo(AccountStatus.SUSPENDED);  // 상태 검증

        verify(accountRepository).save(any(Account.class));
    }

    // ======================================================================================
    // FAIL 1: 가족 관계 없으면 ACCESS_DENIED
    // ======================================================================================
    @Test
    @DisplayName("FAIL - 가족 관계 없으면 ACCESS_DENIED 예외 발생")
    void createGoalAccount_fail_noRelationship() {
        Long parentId = 1L;
        Long childId = 2L;

        GoalAccountCreateReq req = new GoalAccountCreateReq(childId, "목표");

        given(relationshipRepository.existsByParent_IdAndChild_Id(parentId, childId))
                .willReturn(false);

        assertThatThrownBy(() -> goalAccountService.createGoalAccount(parentId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorAuthCode.ACCESS_DENIED.getMessage());

        verify(userRepository, never()).findById(any());
        verify(accountRepository, never()).save(any());
    }

    // ======================================================================================
    // FAIL 2: 자녀 정보 없음 → USER_NOT_FOUND
    // ======================================================================================
    @Test
    @DisplayName("FAIL - 자녀 CoreUser 없음 → USER_NOT_FOUND")
    void createGoalAccount_fail_childNotFound() {
        Long parentId = 1L;
        Long childId = 2L;

        GoalAccountCreateReq req = new GoalAccountCreateReq(childId,"목표");

        given(relationshipRepository.existsByParent_IdAndChild_Id(parentId, childId))
                .willReturn(true);

        given(userRepository.findById(childId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> goalAccountService.createGoalAccount(parentId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.USER_NOT_FOUND.getMessage());

        verify(accountRepository, never()).save(any());
    }

    // ======================================================================================
    // FAIL 3: save() 호출되지 않으면 계좌 생성 실패
    // ======================================================================================
    @Test
    @DisplayName("FAIL - save() 호출되지 않으면 계좌가 생성되지 않은 것으로 본다")
    void createGoalAccount_fail_notSaved() {
        Long parentId = 1L;
        Long childId = 2L;

        GoalAccountCreateReq req = new GoalAccountCreateReq(childId, "목표");

        given(relationshipRepository.existsByParent_IdAndChild_Id(parentId, childId))
                .willReturn(true);

        CoreUser child = CoreUser.builder().name("자녀").build();
        ReflectionTestUtils.setField(child, "id", childId);

        given(userRepository.findById(childId)).willReturn(Optional.of(child));

        // save() 실행 시 null 반환 (비정상)
        given(accountRepository.save(any(Account.class))).willReturn(null);

        Account result = goalAccountService.createGoalAccount(parentId, req);

        assertThat(result).isNull();   // 계좌 미생성 (Fail case)

        verify(accountRepository).save(any(Account.class));
    }
}
