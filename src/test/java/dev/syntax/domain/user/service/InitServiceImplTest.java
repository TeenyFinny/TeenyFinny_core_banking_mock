package dev.syntax.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.account.service.BalanceService;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.ChildUserInitRes;
import dev.syntax.domain.user.dto.ParentUserInitRes;
import dev.syntax.domain.user.dto.UserInitRes;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * InitService 구현체 통합 테스트
 * 자녀/부모 사용자 생성 테스트를 모두 포함합니다.
 */
@ExtendWith(MockitoExtension.class)
class InitServiceImplTest {

    @Mock
    private CoreUserRepository coreUserRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private InitServiceImpl initService;

    private ChannelUserInitReq childReq;
    private ChannelUserInitReq parentReq;

    private CoreUser parent;
    private CoreUser child;

    @BeforeEach
    void setUp() {

        parentReq = new ChannelUserInitReq(
                1L,
                Role.PARENT,
                "부모",
                "010-1234-5678",
                LocalDate.of(1980, 1, 1)
        );

        childReq = new ChannelUserInitReq(
                2L,
                Role.CHILD,
                "자녀",
                "010-9876-5432",
                LocalDate.of(2010, 1, 1)
        );

        parent = CoreUser.builder()
                .id(1L)
                .channelUserId(1L)
                .name("부모")
                .phoneNumber("010-1234-5678")
                .birthDate(LocalDate.of(1980, 1, 1))
                .build();

        child = CoreUser.builder()
                .id(2L)
                .channelUserId(2L)
                .name("자녀")
                .phoneNumber("010-9876-5432")
                .birthDate(LocalDate.of(2010, 1, 1))
                .build();
    }


    // -------------------------------------------------------------
    // 1) 자녀 사용자 생성 테스트
    // -------------------------------------------------------------
    @Nested
    @DisplayName("자녀 사용자 생성 테스트")
    class ChildUserCreationTest {

        @Test
        @DisplayName("자녀 사용자 생성 성공")
        void initChildUser_success() {
            // given
            given(coreUserRepository.existsByChannelUserId(childReq.channelUserId()))
                    .willReturn(false);

            // 저장 시 ID 부여
            given(coreUserRepository.save(any(CoreUser.class)))
                    .willAnswer(invocation -> {
                        CoreUser u = invocation.getArgument(0);
                        return CoreUser.builder()
                                .id(2L)
                                .channelUserId(u.getChannelUserId())
                                .name(u.getName())
                                .phoneNumber(u.getPhoneNumber())
                                .birthDate(u.getBirthDate())
                                .build();
                    });

            // when
            UserInitRes result = initService.initChannelUser(childReq);

            // then
            assertThat(result).isInstanceOf(ChildUserInitRes.class);
            ChildUserInitRes res = (ChildUserInitRes) result;
            assertThat(res.coreUserId()).isEqualTo(2L);

            verify(coreUserRepository).save(any(CoreUser.class));
            verify(accountService, never()).createDepositAccount(any());
            verify(balanceService, never()).deposit(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("자녀 사용자 생성 실패 - 중복 사용자")
        void initChildUser_fail_duplicate() {
            // given
            given(coreUserRepository.existsByChannelUserId(childReq.channelUserId()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> initService.initChannelUser(childReq))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.CONFLICT.getMessage());

            verify(coreUserRepository, never()).save(any(CoreUser.class));
        }
    }


    // -------------------------------------------------------------
    // 2) 부모 사용자 생성 테스트
    // -------------------------------------------------------------
    @Nested
    @DisplayName("부모 사용자 생성 테스트")
    class ParentUserCreationTest {

        @Test
        @DisplayName("부모 사용자 생성 성공 - CoreUser 생성 + DEPOSIT 계좌 생성 + 초기입금")
        void initParentUser_success() {
            // given
            given(coreUserRepository.existsByChannelUserId(parentReq.channelUserId()))
                    .willReturn(false);

            // CoreUser 저장 (ID 자동 생성된 것처럼 반환)
            given(coreUserRepository.save(any(CoreUser.class)))
                    .willAnswer(invocation -> {
                        CoreUser u = invocation.getArgument(0);
                        return CoreUser.builder()
                                .id(1L)
                                .channelUserId(u.getChannelUserId())
                                .name(u.getName())
                                .phoneNumber(u.getPhoneNumber())
                                .birthDate(u.getBirthDate())
                                .build();
                    });

            // 계좌 생성 Mock
            Account createdAccount = new Account();
            ReflectionTestUtils.setField(createdAccount, "id", 10L);

            given(accountService.createDepositAccount(any()))
                    .willReturn(createdAccount);

            // 초기 입금 deposit mock
            doNothing().when(balanceService).deposit(
                    eq(10L),
                    any(CoreUser.class),
                    eq(new BigDecimal("1000000")),
                    eq("초기 잔액"),
                    eq(TransactionCategory.ETC),
                    isNull(),
                    eq(TransactionCode.DEPOSIT)
            );

            // when
            UserInitRes result = initService.initChannelUser(parentReq);

            // then
            assertThat(result).isInstanceOf(ParentUserInitRes.class);
            ParentUserInitRes res = (ParentUserInitRes) result;

            assertThat(res.coreUserId()).isEqualTo(1L);
            assertThat(res.account().accountId()).isEqualTo(10L);

            verify(coreUserRepository).save(any(CoreUser.class));
            verify(accountService).createDepositAccount(any(CoreUser.class));

            verify(balanceService).deposit(
                    eq(10L),
                    any(CoreUser.class),
                    eq(new BigDecimal("1000000")),
                    eq("초기 잔액"),
                    eq(TransactionCategory.ETC),
                    isNull(),
                    eq(TransactionCode.DEPOSIT)
            );
        }

        @Test
        @DisplayName("부모 사용자 생성 실패 - 이미 등록된 사용자")
        void initParentUser_fail_duplicate() {
            // given
            given(coreUserRepository.existsByChannelUserId(parentReq.channelUserId()))
                    .willReturn(true);  // 이미 사용자 존재

            // when & then
            assertThatThrownBy(() -> initService.initChannelUser(parentReq))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.CONFLICT.getMessage());

            verify(coreUserRepository).existsByChannelUserId(parentReq.channelUserId());
            verify(coreUserRepository, never()).save(any(CoreUser.class));
            verify(accountService, never()).createDepositAccount(any());
            verify(balanceService, never()).deposit(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
