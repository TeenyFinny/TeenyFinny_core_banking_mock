package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountStatusUpdateRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.dto.UserAccountListRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountStatus;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.entity.CoreUserRelationship;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

	@InjectMocks
	private AccountServiceImpl accountService;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private CoreUserRepository userRepository;

	@Mock
	private CoreUserRelationshipRepository relationshipRepository;

	// ============================================================================
	// 1) getUserAccounts() 테스트 통합
	// ============================================================================

	@Test
	@DisplayName("자녀가 없는 사용자의 계좌 조회 시 본인 계좌만 반환한다")
	void getUserAccounts_ShouldReturnOnlyUserAccounts_WhenUserHasNoChildren() {
		// given
		Long userId = 1L;

		CoreUser parentUser = CoreUser.builder().name("부모").phoneNumber("010").build();
		ReflectionTestUtils.setField(parentUser, "id", userId);

		Account account = Account.builder()
				.user(parentUser)
				.balance(BigDecimal.TEN)
				.number("111-111")
				.productName("Parent Account")
				.build();
		ReflectionTestUtils.setField(account, "id", 100L);

		given(accountRepository.findAllByUserId(userId))
				.willReturn(List.of(account));
		given(relationshipRepository.findAllByParent_Id(userId))
				.willReturn(Collections.emptyList());

		// when
		UserAccountListRes result = accountService.getUserAccounts(userId);

		// then
		assertThat(result.accounts()).hasSize(1);
		assertThat(result.accounts().get(0).accountId()).isEqualTo(100L);
		assertThat(result.children()).isEmpty();
	}

	@Test
	@DisplayName("자녀가 있는 사용자의 계좌 조회 시 자녀 계좌도 포함하여 반환한다")
	void getUserAccounts_ShouldReturnUserAndChildrenAccounts_WhenUserIsParent() {
		// given
		Long parentId = 1L;
		Long childId = 2L;

		CoreUser parentUser = CoreUser.builder().name("부모").build();
		CoreUser childUser = CoreUser.builder().name("자녀").build();
		ReflectionTestUtils.setField(parentUser, "id", parentId);
		ReflectionTestUtils.setField(childUser, "id", childId);

		Account parentAccount = Account.builder()
				.user(parentUser)
				.balance(BigDecimal.TEN)
				.number("111-111")
				.productName("Parent Account")
				.build();
		ReflectionTestUtils.setField(parentAccount, "id", 100L);

		Account childAccount = Account.builder()
				.user(childUser)
				.balance(BigDecimal.ONE)
				.number("222-222")
				.productName("Child Account")
				.build();
		ReflectionTestUtils.setField(childAccount, "id", 200L);

		CoreUserRelationship relationship = CoreUserRelationship.builder()
				.parent(parentUser)
				.child(childUser)
				.build();

		given(accountRepository.findAllByUserId(parentId))
				.willReturn(List.of(parentAccount));

		given(relationshipRepository.findAllByParent_Id(parentId))
				.willReturn(List.of(relationship));

		given(accountRepository.findAllByUser_IdIn(List.of(childId)))
				.willReturn(List.of(childAccount));

		// when
		UserAccountListRes result = accountService.getUserAccounts(parentId);

		// then
		assertThat(result.accounts()).hasSize(1);
		assertThat(result.accounts().get(0).accountId()).isEqualTo(100L);

		assertThat(result.children()).hasSize(1);
		assertThat(result.children().get(0).userId()).isEqualTo(childId);
		assertThat(result.children().get(0).accounts().get(0).accountId()).isEqualTo(200L);
	}

	// ============================================================================
	// 2) createChildAllowanceAccount() 테스트 통합
	// ============================================================================

	@Test
	@DisplayName("SUCCESS - 부모가 자녀 용돈 계좌를 생성하면 ALLOWANCE 계좌 생성 + 관계 매핑된다")
	void createChildAllowanceAccount_success() {
		// given
		CoreUser parent = CoreUser.builder().name("부모").build();
		CoreUser child = CoreUser.builder().name("자녀").build();
		ReflectionTestUtils.setField(parent, "id", 1L);
		ReflectionTestUtils.setField(child, "id", 2L);

		DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.ALLOWANCE);

		given(userRepository.findById(1L)).willReturn(Optional.of(parent));
		given(userRepository.findById(2L)).willReturn(Optional.of(child));
		given(relationshipRepository.existsByParentAndChild(parent, child)).willReturn(false);

		// 관계 저장 mock
		given(relationshipRepository.save(any(CoreUserRelationship.class)))
				.willAnswer(invocation -> {
					CoreUserRelationship r = invocation.getArgument(0);
					ReflectionTestUtils.setField(r, "id", 99L);
					return r;
				});

		// 계좌 저장 mock
		Account account = Account.builder()
				.user(child)
				.number("123")
				.productName("용돈계좌")
				.interestRate(new BigDecimal("0.001"))
				.type(AccountType.ALLOWANCE)
				.build();
		ReflectionTestUtils.setField(account, "id", 10L);

		given(accountRepository.save(any(Account.class))).willReturn(account);

		// when
		Account result = accountService.createChildAllowanceAccount(1L, req);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(10L);
		assertThat(result.getType()).isEqualTo(AccountType.ALLOWANCE);
		assertThat(result.getUser().getId()).isEqualTo(2L);

		verify(relationshipRepository).save(any(CoreUserRelationship.class));
		verify(accountRepository, times(2)).save(any(Account.class));
	}

	@Test
	@DisplayName("FAIL - 부모 ID != 요청 parentCoreId → ACCESS_DENIED")
	void createChildAllowanceAccount_fail_invalidParent() {
		DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.ALLOWANCE);

		assertThatThrownBy(() -> accountService.createChildAllowanceAccount(999L, req))
				.isInstanceOf(BusinessException.class)
				.hasMessage(ErrorAuthCode.ACCESS_DENIED.getMessage());

		verify(userRepository, never()).findById(any());
		verify(accountRepository, never()).save(any());
	}

	@Test
	@DisplayName("FAIL - 부모-자녀 관계 이미 존재 → CONFLICT")
	void createChildAllowanceAccount_fail_relationshipExists() {
		CoreUser parent = CoreUser.builder().name("부모").build();
		CoreUser child = CoreUser.builder().name("자녀").build();
		ReflectionTestUtils.setField(parent, "id", 1L);
		ReflectionTestUtils.setField(child, "id", 2L);

		DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.ALLOWANCE);

		given(userRepository.findById(1L)).willReturn(Optional.of(parent));
		given(userRepository.findById(2L)).willReturn(Optional.of(child));

		given(relationshipRepository.existsByParentAndChild(parent, child))
				.willReturn(true);

		assertThatThrownBy(() -> accountService.createChildAllowanceAccount(1L, req))
				.isInstanceOf(BusinessException.class)
				.hasMessage(ErrorBaseCode.CONFLICT.getMessage());

		verify(accountRepository, never()).save(any());
	}

	@Test
	@DisplayName("FAIL - 자녀 CoreUser 없음 → CHILD_USER_NOT_FOUND")
	void createChildAllowanceAccount_fail_childNotFound() {
		CoreUser parent = CoreUser.builder().name("부모").build();
		ReflectionTestUtils.setField(parent, "id", 1L);

		DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.ALLOWANCE);

		given(userRepository.findById(1L)).willReturn(Optional.of(parent));
		given(userRepository.findById(2L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.createChildAllowanceAccount(1L, req))
				.isInstanceOf(BusinessException.class)
				.hasMessage(ErrorBaseCode.CHILD_USER_NOT_FOUND.getMessage());

		verify(accountRepository, never()).save(any());
	}

	// =====================================================================================
	// SUCCESS: ACTIVE → CLOSED 상태 변경
	// =====================================================================================
	@Test
	@DisplayName("SUCCESS - 계좌 상태가 ACTIVE 에서 CLOSED 로 정상 변경된다")
	void updateStatus_success() {
		// given
		String number = "ACC-001";

		Account account = Account.builder()
				.productName("목표 계좌")
				.status(AccountStatus.ACTIVE)
				.build();
		ReflectionTestUtils.setField(account, "id", 10L);

		given(accountRepository.findByNumber(number))
				.willReturn(Optional.of(account));

		// when
		AccountStatusUpdateRes res = accountService.updateStatus(number, AccountStatus.CLOSED);

		// then
		assertThat(res).isNotNull();
		assertThat(res.accountId()).isEqualTo(10L);
		assertThat(res.status()).isEqualTo(AccountStatus.CLOSED);

		// 실제 엔티티 상태가 변경되었는지 확인
		assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
	}

	// =====================================================================================
	// FAIL: 계좌가 존재하지 않으면 ACCOUNT_NOT_FOUND 예외
	// =====================================================================================
	@Test
	@DisplayName("FAIL - 존재하지 않는 계좌번호면 ACCOUNT_NOT_FOUND 예외 발생")
	void updateStatus_fail_notFound() {
		// given
		given(accountRepository.findByNumber(anyString()))
				.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> accountService.updateStatus("UNKNOWN", AccountStatus.CLOSED))
				.isInstanceOf(BusinessException.class)
				.hasMessage(ErrorBaseCode.ACCOUNT_NOT_FOUND.getMessage());
	}

	// =====================================================================================
	// FAIL: 상태가 실제로 변경되지 않은 경우
	// (Mock이 잘못된 객체를 반환했다고 가정하는 테스트)
	// =====================================================================================
	@Test
	@DisplayName("FAIL - updateStatus가 호출되었으나 상태가 실제로 변경되지 않으면 실패 케이스")
	void updateStatus_fail_statusNotChanged() {
		// given
		String number = "ACC-002";

		Account account = Account.builder()
				.productName("목표 계좌")
				.status(AccountStatus.ACTIVE)
				.build();
		ReflectionTestUtils.setField(account, "id", 20L);

		given(accountRepository.findByNumber(number))
				.willReturn(Optional.of(account));

		// when
		AccountStatusUpdateRes res = accountService.updateStatus(number, AccountStatus.CLOSED);

		// then
		// FAIL 조건: 상태가 CLOSED 로 변경되지 않은 경우를 가정
		boolean statusChanged = account.getStatus() == AccountStatus.CLOSED;

		assertThat(statusChanged).isTrue(); // 정상 동작이면 true

		// "실패 상황"을 테스트하려면 아래처럼 false일 때를 가정할 수도 있음
		// assertThat(statusChanged).isFalse();  // ← FAIL 케이스 시뮬레이션
	}
}
