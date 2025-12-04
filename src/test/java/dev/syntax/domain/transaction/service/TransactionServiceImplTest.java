package dev.syntax.domain.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.dto.TransactionAllowanceHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionAllowanceItemRes;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.repository.TransactionRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    // =========================================================================================
    // SUCCESS: 특정 기간 거래 조회 성공
    // =========================================================================================
    @Test
    @DisplayName("SUCCESS - 특정 기간의 거래 내역 조회 성공")
    void getHistoryByPeriod_success() {

        // given
        String number = "123-123";
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        // 계좌 소유 사용자 (필수)
        CoreUser owner = CoreUser.builder()
                .id(1L)        // requesterId = 1L 과 동일하게 설정하면 바로 권한 통과
                .build();

        Account account = Account.builder()
                .number(number)
                .balance(new BigDecimal("50000"))
                .user(owner)
                .build();
        // ID 세팅
        ReflectionTestUtils.setField(account, "id", 10L);

        given(accountRepository.findByNumber(number))
                .willReturn(Optional.of(account));

        Transaction t1 = Transaction.builder()
                .id(1L)
                .merchantName("편의점")
                .amount(new BigDecimal("5000"))
                .code("PAYMENT")
                .category(TransactionCategory.ETC)
                .transactionDate(LocalDateTime.of(2025, 1, 10, 10, 30))
                .balanceAfter(new BigDecimal("45000"))
                .build();

        Transaction t2 = Transaction.builder()
                .id(2L)
                .merchantName("카페")
                .amount(new BigDecimal("3000"))
                .code("PAYMENT")
                .category(TransactionCategory.FOOD)
                .transactionDate(LocalDateTime.of(2025, 1, 20, 14, 00))
                .balanceAfter(new BigDecimal("42000"))
                .build();

        given(transactionRepository.findHistoryByPeriod(
                eq(10L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(List.of(t1, t2));

        // when
        TransactionAllowanceHistoryRes res =
                transactionService.getHistoryByPeriod(1L, number, start, end);

        // then
        assertThat(res).isNotNull();
        assertThat(res.balance()).isEqualTo(new BigDecimal("50000"));

        assertThat(res.transactions()).hasSize(2);

        TransactionAllowanceItemRes item1 = res.transactions().get(0);
        assertThat(item1.merchantName()).isEqualTo("편의점");
        assertThat(item1.amount()).isEqualTo(new BigDecimal("5000"));

        TransactionAllowanceItemRes item2 = res.transactions().get(1);
        assertThat(item2.merchantName()).isEqualTo("카페");

        verify(accountRepository).findByNumber(number);
        verify(transactionRepository).findHistoryByPeriod(eq(10L), any(), any());
    }

    // =========================================================================================
    // FAIL: 계좌가 없는 경우 예외 발생
    // =========================================================================================
    @Test
    @DisplayName("FAIL - 계좌번호가 존재하지 않으면 NOT_FOUND_ENTITY 예외 발생")
    void getHistoryByPeriod_fail_accountNotFound() {

        // given
        String number = "999-999";
        given(accountRepository.findByNumber(number))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                transactionService.getHistoryByPeriod(1L, number, LocalDate.now(), LocalDate.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());

        verify(transactionRepository, never()).findHistoryByPeriod(any(), any(), any());
    }

    // =========================================================================================
    // SUCCESS: 조회 기간 내 거래가 없는 경우 빈 리스트 반환
    // =========================================================================================
    @Test
    @DisplayName("SUCCESS - 거래가 없는 기간 조회 시 빈 리스트 반환")
    void getHistoryByPeriod_success_emptyList() {

        // given
        String number = "123-123";

        // 계좌 소유 사용자 (필수)
        CoreUser owner = CoreUser.builder()
                .id(1L)        // requesterId = 1L 과 동일하게 설정하면 바로 권한 통과
                .build();

        Account account = Account.builder()
                .number(number)
                .balance(new BigDecimal("10000"))
                .user(owner)
                .build();
        ReflectionTestUtils.setField(account, "id", 10L);

        given(accountRepository.findByNumber(number))
                .willReturn(Optional.of(account));

        given(transactionRepository.findHistoryByPeriod(eq(10L), any(), any()))
                .willReturn(List.of()); // empty list

        // when
        TransactionAllowanceHistoryRes res =
                transactionService.getHistoryByPeriod(1L, number, LocalDate.now(), LocalDate.now());

        // then
        assertThat(res.transactions()).isEmpty();
        assertThat(res.balance()).isEqualTo(new BigDecimal("10000"));
    }
}
