package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.dto.ChildAccountInfoRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.dto.UserAccountListRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CoreUserRepository coreUserRepository;
    private final CoreUserRelationshipRepository relationshipRepository;

    @Override
    public UserAccountListRes getUserAccounts(Long coreUserId) {
        // 1. 본인 계좌 조회
        List<AccountItemRes> myAccounts = accountRepository.findAllByUserId(coreUserId).stream()
                .map(AccountItemRes::from)
                .toList();

        // 2. 자녀 계좌 조회 (부모일 경우)
        List<Long> childIds = relationshipRepository.findAllByParent_Id(coreUserId).stream()
                .map(relationship -> relationship.getChild().getId())
                .toList();

        if (childIds.isEmpty()) {
            return new UserAccountListRes(myAccounts, Collections.emptyList());
        }

        List<Account> allChildAccounts = accountRepository.findAllByUser_IdIn(childIds);

        Map<Long, List<AccountItemRes>> childAccountsByChildId = allChildAccounts.stream()
                .collect(Collectors.groupingBy(
                        account -> account.getUser().getId(),
                        Collectors.mapping(AccountItemRes::from, Collectors.toList())
                ));

        List<ChildAccountInfoRes> children = childIds.stream()
                .map(childId -> new ChildAccountInfoRes(childId, childAccountsByChildId.getOrDefault(childId, Collections.emptyList())))
                .toList();

        return new UserAccountListRes(myAccounts, children);
    }

    @Transactional
    @Override
    public Account createDepositAccount(CoreUser user) {
        Account account = Account.builder()
                .user(user)
                .number(AccountNumberGenerator.generate())
                .productName("입출금 통장")
                .interestRate(new BigDecimal("0.001")) // 0.1%
                .type(AccountType.DEPOSIT)
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public Account createChildDepositAccount(Long id, DepositAccountReq req) {
        if (!Objects.equals(id, req.parentCoreId())) {
            throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
        }
        CoreUser child = createFamilyRelationship(req);
        return createDepositAccount(child);
    }

    /**
     * 가족 관계를 생성합니다.
     * <p>
     * 부모-자녀 간 가족 관계를 매핑합니다. 이미 등록된 관계인 경우 예외를 발생시킵니다.
     * </p>
     *
     * @param req 가족 관계 생성 요청 정보 (부모 ID와 자녀 ID 포함)
     * @return 가족 관계가 매핑된 자녀 CoreUser 엔티티
     * @throws dev.syntax.global.exception.BusinessException 부모 또는 자녀를 찾을 수 없는 경우, 이미 가족 관계가 등록된 경우
     */
    private CoreUser createFamilyRelationship(DepositAccountReq req) {
        // 부모 CoreUser 조회
        CoreUser parent = coreUserRepository.findById(req.parentCoreId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.PARENT_USER_NOT_FOUND));

        // 자녀 CoreUser 조회
        CoreUser child = coreUserRepository.findById(req.childCoreId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.CHILD_USER_NOT_FOUND));

        // 기존 가족 관계 확인
        boolean relationshipExists = relationshipRepository.existsByParentAndChild(parent, child);
        if (relationshipExists) {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        }

        // 가족 관계 매핑
        dev.syntax.domain.user.entity.CoreUserRelationship relationship = dev.syntax.domain.user.entity.CoreUserRelationship.builder()
                .parent(parent)
                .child(child)
                .build();
        relationshipRepository.save(relationship);

        return child;
    }
}
