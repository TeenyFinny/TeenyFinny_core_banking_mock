package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.dto.DepositAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.util.AccountNumberGenerator;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRelationshipRepository;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.domain.user.service.InitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final CoreUserRepository coreUserRepository;
    private final CoreUserRelationshipRepository relationshipRepository;
    private final AccountRepository accountRepository;
    private final InitService initService;

    @Override
    public List<AccountItemRes> getUserAccounts(Long coreUserId) {
        List<Account> accounts = accountRepository.findAllByUserId(coreUserId);

        return accounts.stream()
                .map(AccountItemRes::from)
                .toList();
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
    public Account createChildDepositAccount(DepositAccountReq req) {
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
                .orElseThrow(() -> new dev.syntax.global.exception.BusinessException(dev.syntax.global.response.error.ErrorBaseCode.PARENT_USER_NOT_FOUND));

        // 자녀 CoreUser 조회
        CoreUser child = coreUserRepository.findById(req.childCoreId())
                .orElseThrow(() -> new dev.syntax.global.exception.BusinessException(dev.syntax.global.response.error.ErrorBaseCode.CHILD_USER_NOT_FOUND));

        // 기존 가족 관계 확인
        boolean relationshipExists = relationshipRepository.existsByParentAndChild(parent, child);
        if (relationshipExists) {
            throw new dev.syntax.global.exception.BusinessException(dev.syntax.global.response.error.ErrorBaseCode.CONFLICT);
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
