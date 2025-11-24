package dev.syntax.domain.user.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
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
import dev.syntax.domain.user.entity.CoreUserRelationship;
import dev.syntax.domain.user.enums.Role;
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

/**
 * 사용자 생성 서비스 구현체
 * <p>
 * 부모 사용자 가입 시 CoreUser 생성, 계좌 생성, 초기 잔액 입금을 처리합니다.
 * 자녀 사용자 가입 시 CoreUser를 생성합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitServiceImpl implements InitService {

    private final CoreUserRepository coreUserRepository;
    private final CoreUserRelationshipRepository coreUserRelationshipRepository;
    private final AccountService accountService;
    private final BalanceService balanceService;

    private static final BigDecimal INITIAL_DEPOSIT_AMOUNT = new BigDecimal("1000000");
    private static final String INITIAL_DEPOSIT_MERCHANT_NAME = "초기 잔액";

    /**
     * 사용자 생성을 처리합니다.
     * <p>
     * 요청의 Role에 따라 부모와 자녀를 구분하여 처리합니다.
     * </p>
     */
    @Transactional
    @Override
    public UserInitRes initChannelUser(ChannelUserInitReq req) {
        Role role = req.role();

        return switch (role) {
            case PARENT -> initParentUser(req);
            case CHILD -> initChildUser(req);
            default -> throw new BusinessException(ErrorAuthCode.UNAUTHORIZED);
        };
    }

    /**
     * 부모 사용자 생성 처리합니다.
     */
    private ParentUserInitRes initParentUser(ChannelUserInitReq req) {
        CoreUser user = registerUser(req);

        // 계좌 생성
        Account newAccount = accountService.createDepositAccount(user);

        balanceService.deposit(
                newAccount.getId(),
                user,
                INITIAL_DEPOSIT_AMOUNT, // 100만원 입금
                INITIAL_DEPOSIT_MERCHANT_NAME,
                TransactionCategory.ETC,
                null,
                TransactionCode.DEPOSIT
        );

        // 계좌 정보를 AccountItemRes로 변환
        AccountItemRes accountRes = AccountItemRes.from(newAccount);

        // 반환
        return ParentUserInitRes.from(user, accountRes);
    }

    /**
     * 자녀 사용자 초기화를 처리합니다.
     */
    private ChildUserInitRes initChildUser(ChannelUserInitReq req) {
        CoreUser user = registerUser(req);

        // 반환
        return new ChildUserInitRes(user.getId());
    }

    /**
     * CoreUser를 생성하고 저장합니다.
     * <p>
     * 채널 사용자 ID로 중복 여부를 확인하고, 중복되지 않은 경우 새로운 CoreUser를 생성하여 저장합니다.
     * 계좌 생성 및 가족 관계 등록은 이 메서드에서 처리하지 않습니다.
     * </p>
     *
     * @param req 사용자 생성 요청 정보
     * @return 생성된 CoreUser 엔티티
     * @throws BusinessException 이미 등록된 사용자인 경우 (CONFLICT)
     */
    private CoreUser registerUser(ChannelUserInitReq req) {

        // 기존에 등록된 유저인지 확인
        boolean exist = coreUserRepository.existsByChannelUserId(req.channelUserId());
        if (exist) {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        }

        // CoreUser 생성 및 저장 (계좌 생성 및 가족 관계 등록 안함)
        CoreUser user = CoreUser.builder()
                .channelUserId(req.channelUserId())
                .name(req.name())
                .phoneNumber(req.phoneNumber())
                .birthDate(req.birthDate())
                .build();

        return coreUserRepository.save(user);
    }

    /**
     * 가족 관계를 생성합니다.
     * <p>
     * 부모-자녀 간 가족 관계를 매핑합니다. 이미 등록된 관계인 경우 예외를 발생시킵니다.
     * </p>
     * <ul>
     *   <li>부모 CoreUser 조회 및 검증</li>
     *   <li>자녀 CoreUser 조회 및 검증</li>
     *   <li>기존 가족 관계 중복 확인</li>
     *   <li>가족 관계 매핑 (CoreUserRelationship 생성 및 저장)</li>
     * </ul>
     *
     * @param req 가족 관계 생성 요청 정보 (부모 ID와 자녀 ID 포함)
     * @return 가족 관계가 매핑된 자녀 CoreUser 엔티티
     * @throws BusinessException 부모 또는 자녀를 찾을 수 없는 경우 (USER_NOT_FOUND)
     * @throws BusinessException 이미 가족 관계가 등록된 경우 (CONFLICT)
     */
    @Transactional
    @Override
    public CoreUser createFamilyRelationship(DepositAccountReq req) {
        // 부모 CoreUser 조회
        CoreUser parent = coreUserRepository.findById(req.parentCoreId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 자녀 CoreUser 조회
        CoreUser child = coreUserRepository.findById(req.childCoreId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 기존 가족 관계 확인
        boolean relationshipExists = coreUserRelationshipRepository.existsByParentAndChild(parent, child);
        if (relationshipExists) {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        }

        // 가족 관계 매핑
        CoreUserRelationship relationship = CoreUserRelationship.builder()
                .parent(parent)
                .child(child)
                .build();
        coreUserRelationshipRepository.save(relationship);
        log.info("[가족관계 매핑 완료] parentId: {}, childId: {}", parent.getId(), child.getId());

        // 자녀 반환
        return child;
    }
}
