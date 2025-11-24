package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.entity.CoreUserRelationship;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private dev.syntax.domain.user.repository.CoreUserRepository coreUserRepository;

    @Mock
    private CoreUserRelationshipRepository coreUserRelationshipRepository;

    @Test
    @DisplayName("자녀가 없는 사용자의 계좌 조회 시 본인 계좌만 반환한다")
    void getUserAccounts_ShouldReturnOnlyUserAccounts_WhenUserHasNoChildren() {
        // given
        Long userId = 1L;
        Account account = Account.builder()
                .id(100L)
                .balance(BigDecimal.TEN)
                .productName("Test Account")
                .number("123-456")
                .build();

        given(accountRepository.findAllByUserId(userId)).willReturn(List.of(account));
        given(coreUserRelationshipRepository.findAllByParent_Id(userId)).willReturn(Collections.emptyList());

        // when
        dev.syntax.domain.account.dto.UserAccountListRes result = accountService.getUserAccounts(userId);

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

        Account parentAccount = Account.builder()
                .id(100L)
                .balance(BigDecimal.TEN)
                .productName("Parent Account")
                .number("111-111")
                .build();

        Account childAccount = Account.builder()
                .id(200L)
                .balance(BigDecimal.ONE)
                .productName("Child Account")
                .number("222-222")
                .build();

        CoreUser childUser = CoreUser.builder().id(childId).build();
        CoreUserRelationship relationship = CoreUserRelationship.builder()
                .child(childUser)
                .build();

        given(accountRepository.findAllByUserId(parentId)).willReturn(List.of(parentAccount));
        given(coreUserRelationshipRepository.findAllByParent_Id(parentId)).willReturn(List.of(relationship));
        given(accountRepository.findAllByUserId(childId)).willReturn(List.of(childAccount));

        // when
        dev.syntax.domain.account.dto.UserAccountListRes result = accountService.getUserAccounts(parentId);

        // then
        assertThat(result.accounts()).hasSize(1);
        assertThat(result.accounts().get(0).accountId()).isEqualTo(100L);

        assertThat(result.children()).hasSize(1);
        assertThat(result.children().get(0).userId()).isEqualTo(childId);
        assertThat(result.children().get(0).accounts()).hasSize(1);
        assertThat(result.children().get(0).accounts().get(0).accountId()).isEqualTo(200L);
    }
}
