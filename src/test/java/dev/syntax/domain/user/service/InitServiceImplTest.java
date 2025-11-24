package dev.syntax.domain.user.service;

import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.account.service.BalanceService;
import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.ChildUserInitRes;
import dev.syntax.domain.user.dto.UserInitRes;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.entity.CoreUserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * InitService 구현체 테스트
 * <p>
 * 자녀 사용자 생성 및 가족 관계 생성 기능을 테스트합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class InitServiceImplTest {

    @Mock
    private CoreUserRepository coreUserRepository;

    @Mock
    private CoreUserRelationshipRepository coreUserRelationshipRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private InitServiceImpl initService;

    private ChannelUserInitReq childReq;
    private CoreUser parent;
    private CoreUser child;

    /**
     * 테스트에 사용할 공통 데이터를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
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

    @Nested
    @DisplayName("자녀 사용자 생성 테스트")
    class ChildUserCreationTest {

        /**
         * 자녀 사용자 생성 성공 테스트
         * <p>
         * CoreUser만 생성되고 계좌 생성은 수행되지 않습니다.
         * </p>
         */
        @Test
        @DisplayName("자녀 사용자 생성 성공")
        void initChildUser_success() {
            // given
            given(coreUserRepository.existsByChannelUserId(childReq.channelUserId()))
                    .willReturn(false);

            // save가 호출될 때 전달된 객체에 ID를 설정하고 반환
            given(coreUserRepository.save(any(CoreUser.class)))
                    .willAnswer(invocation -> {
                        CoreUser user = invocation.getArgument(0);
                        // Reflection을 사용하거나, Builder로 새 객체를 만들어 반환
                        return CoreUser.builder()
                                .id(2L)
                                .channelUserId(user.getChannelUserId())
                                .name(user.getName())
                                .phoneNumber(user.getPhoneNumber())
                                .birthDate(user.getBirthDate())
                                .build();
                    });

            // when
            UserInitRes result = initService.initChannelUser(childReq);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(ChildUserInitRes.class);
            ChildUserInitRes childResult = (ChildUserInitRes) result;
            assertThat(childResult.coreUserId()).isEqualTo(2L);

            verify(coreUserRepository).existsByChannelUserId(childReq.channelUserId());
            verify(coreUserRepository).save(any(CoreUser.class));
            verify(accountService, never()).createDepositAccount(any());
            verify(balanceService, never()).deposit(any(), any(), any(), any(), any(), any(), any());
        }

        /**
         * 자녀 사용자 생성 실패 테스트 - 중복 사용자
         */
        @Test
        @DisplayName("자녀 사용자 생성 실패 - 이미 등록된 사용자")
        void initChildUser_fail_duplicate() {
            // given
            given(coreUserRepository.existsByChannelUserId(childReq.channelUserId()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> initService.initChannelUser(childReq))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.CONFLICT.getMessage());

            verify(coreUserRepository).existsByChannelUserId(childReq.channelUserId());
            verify(coreUserRepository, never()).save(any(CoreUser.class));
        }
    }

    @Nested
    @DisplayName("가족 관계 생성 테스트")
    class FamilyRelationshipTest {

        /**
         * 가족 관계 생성 성공 테스트
         * <p>
         * 부모-자녀 간 가족 관계가 정상적으로 매핑되는지 확인합니다.
         * </p>
         */
        @Test
        @DisplayName("가족 관계 생성 성공")
        void createFamilyRelationship_success() {
            // given
            DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

            given(coreUserRepository.findById(1L))
                    .willReturn(Optional.of(parent));
            given(coreUserRepository.findById(2L))
                    .willReturn(Optional.of(child));
            given(coreUserRelationshipRepository.existsByParentAndChild(parent, child))
                    .willReturn(false);

            ArgumentCaptor<CoreUserRelationship> relationshipCaptor =
                    ArgumentCaptor.forClass(CoreUserRelationship.class);
            given(coreUserRelationshipRepository.save(any(CoreUserRelationship.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CoreUser result = initService.createFamilyRelationship(req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getName()).isEqualTo("자녀");

            verify(coreUserRepository).findById(1L);
            verify(coreUserRepository).findById(2L);
            verify(coreUserRelationshipRepository).existsByParentAndChild(parent, child);
            verify(coreUserRelationshipRepository).save(relationshipCaptor.capture());

            CoreUserRelationship savedRelationship = relationshipCaptor.getValue();
            assertThat(savedRelationship.getParent().getId()).isEqualTo(1L);
            assertThat(savedRelationship.getChild().getId()).isEqualTo(2L);
        }

        /**
         * 가족 관계 생성 실패 테스트 - 부모를 찾을 수 없음
         */
        @Test
        @DisplayName("가족 관계 생성 실패 - 부모를 찾을 수 없음")
        void createFamilyRelationship_fail_parentNotFound() {
            // given
            DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

            given(coreUserRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> initService.createFamilyRelationship(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.USER_NOT_FOUND.getMessage());

            verify(coreUserRepository).findById(1L);
            verify(coreUserRepository, never()).findById(2L);
            verify(coreUserRelationshipRepository, never()).save(any());
        }

        /**
         * 가족 관계 생성 실패 테스트 - 자녀를 찾을 수 없음
         */
        @Test
        @DisplayName("가족 관계 생성 실패 - 자녀를 찾을 수 없음")
        void createFamilyRelationship_fail_childNotFound() {
            // given
            DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

            given(coreUserRepository.findById(1L))
                    .willReturn(Optional.of(parent));
            given(coreUserRepository.findById(2L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> initService.createFamilyRelationship(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.USER_NOT_FOUND.getMessage());

            verify(coreUserRepository).findById(1L);
            verify(coreUserRepository).findById(2L);
            verify(coreUserRelationshipRepository, never()).save(any());
        }

        /**
         * 가족 관계 생성 실패 테스트 - 이미 존재하는 관계
         */
        @Test
        @DisplayName("가족 관계 생성 실패 - 이미 존재하는 관계")
        void createFamilyRelationship_fail_alreadyExists() {
            // given
            DepositAccountReq req = new DepositAccountReq(1L, 2L, AccountType.DEPOSIT);

            given(coreUserRepository.findById(1L))
                    .willReturn(Optional.of(parent));
            given(coreUserRepository.findById(2L))
                    .willReturn(Optional.of(child));
            given(coreUserRelationshipRepository.existsByParentAndChild(parent, child))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> initService.createFamilyRelationship(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorBaseCode.CONFLICT.getMessage());

            verify(coreUserRepository).findById(1L);
            verify(coreUserRepository).findById(2L);
            verify(coreUserRelationshipRepository).existsByParentAndChild(parent, child);
            verify(coreUserRelationshipRepository, never()).save(any());
        }
    }
}
