package dev.syntax.domain.transaction.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transaction.dto.TransactionAllowanceHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionAllowanceItemRes;
import dev.syntax.domain.transaction.dto.TransactionDetailItemRes;
import dev.syntax.domain.transaction.dto.TransactionHistoryRes;
import dev.syntax.domain.transaction.dto.TransactionItemRes;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.enums.TransactionCategory;
import dev.syntax.domain.transaction.enums.TransactionCode;
import dev.syntax.domain.transaction.enums.TransactionStatus;
import dev.syntax.domain.transaction.enums.TransactionType;
import dev.syntax.domain.transaction.repository.TransactionRepository;
import dev.syntax.domain.user.entity.CoreUser;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * {@link TransactionService}의 구현체
 * <p>
 * 거래 내역을 Transaction 엔티티로 저장하는 기능을 제공합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * 거래 내역을 기록합니다.
     * <p>
     * Transaction 엔티티를 생성하고 현재 시간을 transactionDate로 자동 설정합니다.
     * </p>
     */
    @Transactional
    @Override
    public void record(CoreUser user,
                       Account account,
                       TransactionType type,
                       BigDecimal amount,
                       BigDecimal balanceAfter,
                       String merchantName,
                       TransactionCategory category,
                       TransactionStatus status,
                       TransactionCode code) {
        Transaction t = Transaction.builder()
                .user(user)
                .account(account)
                .code(code.name())
                .type(type != null ? type : null)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .merchantName(merchantName)
                .category(category)
                .status(status)
                .transactionDate(LocalDateTime.now())
                .build();

        transactionRepository.save(t);
    }

    /**
     * 계좌번호로 거래 내역을 조회합니다.
     * <p>
     * 계좌번호로 Account 엔티티를 조회한 후,
     * 해당 계좌의 거래 내역을 최신순으로 정렬하여 반환합니다.
     * </p>
     *
     * @param number 계좌번호
     * @return 거래 내역 리스트 및 계좌 잔액 정보 {@link TransactionHistoryRes}
     * @throws BusinessException 계좌를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionHistoryRes getHistory(String number) {

        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        BigDecimal balance = account.getBalance();

        List<Transaction> transactions =
                transactionRepository.findByNumberOrderByTransactionDateDesc(number);

        List<TransactionItemRes> items = transactions.stream()
                .map(t -> new TransactionItemRes(
                        t.getId(),
                        t.getMerchantName(),
                        t.getAmount(),
                        t.getTransactionDate(),
                        t.getBalanceAfter()
                ))
                .toList();

        return new TransactionHistoryRes(items, balance);
    }

    /**
     * 계좌번호로 특정 기간의 거래 내역을 조회합니다.
     * <p>
     * 계좌번호로 Account 엔티티를 조회한 후,
     * 해당 계좌의 특정 기간 거래 내역을 최신순으로 정렬하여 반환합니다.
     * </p>
     *
     * @param number 계좌번호
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 거래 내역 리스트 및 계좌 잔액 정보 {@link TransactionAllowanceHistoryRes}
     * @throws BusinessException 계좌를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionAllowanceHistoryRes getHistoryByPeriod(String number, LocalDate startDate, LocalDate endDate) {

        Account account = accountRepository.findByNumber(number)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));
        
        BigDecimal balance = account.getBalance();

        // 조회할 기간의 시작과 끝 날짜 계산 (자정 기준)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        // 성능 최적화 쿼리 실행 (account ID로 조회)
        List<Transaction> transactions =
                transactionRepository.findHistoryByPeriod(account.getId(), start, end);
        
        log.info("조회된 거래 건수: {}", transactions.size());

        List<TransactionAllowanceItemRes> items = transactions.stream()
                .map(t -> new TransactionAllowanceItemRes(
                        t.getId(),
                        t.getMerchantName(),
                        t.getAmount(),
                        TransactionCode.valueOf(t.getCode()),
                        t.getTransactionDate(),
                        t.getCategory(),
                        t.getBalanceAfter()
                ))
                .toList();

        log.info(items.toString());
        return new TransactionAllowanceHistoryRes(items, balance);
    }

    /**
     * 거래 ID로 단일 거래의 상세 정보를 조회합니다.
     * <p>
     * 거래 ID로 Transaction 엔티티를 조회하여
     * 거래 타입, 카테고리, 승인 금액 등 상세 정보를 반환합니다.
     * </p>
     *
     * @param transactionId 거래 ID
     * @return 거래 상세 정보 {@link TransactionDetailItemRes}
     * @throws BusinessException 거래를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailItemRes getTransactionDetail(Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 금액 포맷팅 (쉼표 구분)
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#,###");
        String formattedAmount = decimalFormat.format(transaction.getAmount());
        String formattedApproveAmount = decimalFormat.format(transaction.getAmount());
        String formattedBalanceAfter = decimalFormat.format(transaction.getBalanceAfter());

        // 날짜 포맷팅
        java.time.format.DateTimeFormatter dateFormatter = 
                java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        String formattedDate = transaction.getTransactionDate().format(dateFormatter);

        return new TransactionDetailItemRes(
                transaction.getMerchantName(),
                formattedAmount,
                formattedDate,
                transaction.getType(),
                transaction.getCategory(),
                formattedApproveAmount,
                formattedBalanceAfter
        );
    }
}
