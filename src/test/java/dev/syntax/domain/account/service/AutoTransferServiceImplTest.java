package dev.syntax.domain.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.syntax.domain.account.dto.AutoTransferCreateReq;
import dev.syntax.domain.account.dto.AutoTransferCreateRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.repository.AutoTransferRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.domain.user.repository.CoreUserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AutoTransferServiceImplTest {

    @InjectMocks
    private AutoTransferServiceImpl autoTransferService;

    @Mock
    private AutoTransferRepository autoTransferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoreUserRepository coreUserRepository;

    // =========================================================================================
    // SUCCESS: 자동이체 생성
    // =========================================================================================
    @Test
    @DisplayName("SUCCESS - 자동이체 생성 성공 (상태 = PROCESSING)")
    void createAutoTransfer_success() {

        // given
        Long userId = 5L;

        AutoTransferCreateReq req = new AutoTransferCreateReq(
                userId,
                1L, // fromAccountId
                2L, // toAccountId
                new BigDecimal("10000"),
                10,
                "월 자동이체"
        );

        Account from = Account.builder().id(1L).build();
        Account to = Account.builder().id(2L).build();
        CoreUser user = CoreUser.builder().id(5L).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(to));
        when(coreUserRepository.findByChannelUserId(5L)).thenReturn(Optional.of(user));

        // save() 호출 시 AutoTransfer 엔티티를 그대로 반환하도록 stubbing
        when(autoTransferRepository.save(any(AutoTransfer.class)))
                .thenAnswer(invocation -> {
                    AutoTransfer t = invocation.getArgument(0);
                    ReflectionTestUtils.setField(t, "id", 100L);
                    return t;
                });

        // when
        AutoTransferCreateRes res = autoTransferService.createAutoTransfer(userId, req);

        // then
        assertThat(res.autoTransferId()).isEqualTo(100L);

        // 실제 저장된 객체 캡처
        var captor = ArgumentCaptor.forClass(AutoTransfer.class);
        verify(autoTransferRepository).save(captor.capture());
        AutoTransfer saved = captor.getValue();

        // 상태가 PROCESSING 인지 확인
        assertThat(saved.getStatus()).isEqualTo(AutoTransferStatus.PROCESSING);
    }


    // =========================================================================================
    // FAIL: 출금 계좌 없음
    // =========================================================================================
    @Test
    @DisplayName("FAIL - 출금 계좌 없음 → WITHDRAWAL_NOT_FOUND")
    void createAutoTransfer_fail_noFromAccount() {

        AutoTransferCreateReq req = new AutoTransferCreateReq(
                5L,
                99L, // 잘못된 출금 계좌
                2L,
                new BigDecimal("10000"),
                10,
                "메모"
        );

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                autoTransferService.createAutoTransfer(5L, req)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.WITHDRAWAL_NOT_FOUND.getMessage());
    }


    // =========================================================================================
    // FAIL: 입금 계좌 없음
    // =========================================================================================
    @Test
    @DisplayName("FAIL - 입금 계좌 없음 → DEPOSIT_NOT_FOUND")
    void createAutoTransfer_fail_noToAccount() {

        AutoTransferCreateReq req = new AutoTransferCreateReq(
                5L,
                1L,
                200L,  // 잘못된 입금 계좌
                new BigDecimal("10000"),
                10,
                "메모"
        );

        when(accountRepository.findById(1L)).thenReturn(Optional.of(Account.builder().build()));
        when(accountRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                autoTransferService.createAutoTransfer(5L, req)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.DEPOSIT_NOT_FOUND.getMessage());
    }


    // =========================================================================================
    // SUCCESS: 자동이체 삭제
    // =========================================================================================
    @Test
    @DisplayName("SUCCESS - 자동이체 삭제 성공")
    void deleteAutoTransfer_success() {

        Long userId = 5L;
        Long transferId = 10L;

        CoreUser user = CoreUser.builder().id(userId).build();
        AutoTransfer transfer = AutoTransfer.builder().id(transferId).user(user).build();

        when(coreUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(autoTransferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

        autoTransferService.deleteAutoTransfer(userId, transferId);

        verify(autoTransferRepository, times(1)).delete(transfer);
    }


    // =========================================================================================
    // FAIL: 사용자 없음
    // =========================================================================================
    @Test
    @DisplayName("FAIL - 사용자 없음 → USER_NOT_FOUND")
    void deleteAutoTransfer_fail_userNotFound() {

        when(coreUserRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                autoTransferService.deleteAutoTransfer(5L, 10L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.USER_NOT_FOUND.getMessage());
    }


    // =========================================================================================
    // FAIL: 자동이체 없음
    // =========================================================================================
    @Test
    @DisplayName("FAIL - 자동이체 없음 → AUTO_TRANSFER_NOT_FOUND")
    void deleteAutoTransfer_fail_autoTransferNotFound() {

        CoreUser user = CoreUser.builder().id(5L).build();

        when(coreUserRepository.findById(5L)).thenReturn(Optional.of(user));
        when(autoTransferRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                autoTransferService.deleteAutoTransfer(5L, 10L)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND.getMessage());
    }
}
