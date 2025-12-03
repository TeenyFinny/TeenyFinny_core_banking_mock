package dev.syntax.domain.transaction.controller;

import dev.syntax.domain.transaction.dto.FailedTransactionRes;
import dev.syntax.domain.transaction.entity.Transaction;
import dev.syntax.domain.transaction.service.TransactionAdminService;
import dev.syntax.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 거래 내역 관리 컨트롤러
 * <p>
 * 관리자가 실패한 거래를 조회할 수 있는 API를 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/core/banking/admin/transaction")
@RequiredArgsConstructor
public class TransactionAdminController {

    private final TransactionAdminService transactionAdminService;

    /**
     * 실패한 거래 조회
     * <p>
     * 관리자가 모든 실패한 거래를 조회할 수 있습니다.
     * 자동이체 관련 거래만 필터링할 수 있습니다.
     * </p>
     *
     * @param autoTransferOnly true일 경우 자동이체 관련 실패 거래만 조회
     * @param pageable         페이징 정보
     * @return 실패한 거래 목록 (페이징)
     */
	@GetMapping("/failed")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<FailedTransactionRes> getFailedTransactions(
		@RequestParam(defaultValue = "false") boolean autoTransferOnly,
		@PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable
	) {

		Page<Transaction> transactions =
			autoTransferOnly ?
				transactionAdminService.getFailedAutoTransferTransactions(pageable) :
				transactionAdminService.getFailedTransactions(pageable);

		return PageResponse.of(transactions.map(FailedTransactionRes::from));
	}

}
