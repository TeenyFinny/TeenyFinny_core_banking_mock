package dev.syntax.domain.user.service;

import dev.syntax.domain.account.dto.AccountItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.service.AccountService;
import dev.syntax.domain.account.service.BalanceService;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.user.dto.ChannelUserInitReq;
import dev.syntax.domain.user.dto.ChannelUserInitRes;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 사용자 초기화 서비스 구현체
 * <p>
 * 부모 사용자 가입 시 CoreUser 생성, 계좌 생성, 초기 잔액 입금을 처리합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitServiceImpl implements InitService {

    private final CoreUserRepository coreUserRepository;
    private final AccountService accountService;
    private final BalanceService balanceService;
    private final AccountRepository accountRepository;

    /**
     * 부모 사용자 초기화를 처리합니다.
     * <p>
     * 1. 기존 사용자 중복 확인
     * 2. CoreUser 생성 및 저장
     * 3. 입출금 통장 계좌 생성
     * 4. 초기 잔액 100만원 입금
     * </p>
     *
     * @param req 사용자 초기화 요청 정보
     * @return 생성된 사용자 ID와 계좌 정보
     * @throws BusinessException 이미 등록된 사용자인 경우 (CONFLICT)
     */
    @Transactional
    @Override
    public ChannelUserInitRes initChannelParentUser(ChannelUserInitReq req) {

        // 기존에 등록된 유저인지 확인
        boolean exist = coreUserRepository.existsByChannelUserId(req.channelUserId());
        if (exist) {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        }

        // CoreUser 생성 및 저장
        CoreUser coreUser = CoreUser.builder()
                .channelUserId(req.channelUserId())
                .name(req.name())
                .phoneNumber(req.phoneNumber())
                .birthDate(req.birthDate())
                .build();
        coreUserRepository.save(coreUser);

        // 계좌 생성
        Account newAccount = accountService.createDepositAccount(coreUser);

        // 100만원 입금
        BigDecimal initialAmount = new BigDecimal("1000000");
        balanceService.deposit(
                newAccount.getId(),
                coreUser,
                initialAmount,
                "초기 잔액",
                TransactionCategory.ETC,
                null,
                TransactionCode.DEPOSIT
        );

        // 입금 후 최신 계좌 정보 조회 (잔액 반영)
        Account updatedAccount = accountRepository.findById(newAccount.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        // 계좌 정보를 AccountItemRes로 변환
        AccountItemRes accountRes = AccountItemRes.from(updatedAccount);

        // 반환
        return ChannelUserInitRes.from(coreUser, accountRes);
    }
}
