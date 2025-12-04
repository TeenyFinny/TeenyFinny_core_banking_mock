package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.AutoTransferListRes;
import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.service.AutoTransferAdminService;
import dev.syntax.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 관리자용 자동이체 관리 컨트롤러
 * <p>
 * 관리자가 전체 자동이체를 조회하고 수동으로 실행할 수 있는 API를 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/core/banking/admin/auto-transfer")
@RequiredArgsConstructor
public class AutoTransferAdminController {

    private final AutoTransferAdminService autoTransferAdminService;

    /**
     * 전체 자동이체 조회 (페이징, 필터링)
     * <p>
     * 관리자가 모든 자동이체를 조회할 수 있습니다.
     * 상태별, 날짜 범위별 필터링이 가능합니다.
     * </p>
     *
     * @param status    필터링할 상태 (선택사항)
     * @param startDate 시작 날짜 (선택사항)
     * @param endDate   종료 날짜 (선택사항)
     * @param pageable  페이징 정보
     * @return 자동이체 목록 (페이징)
     */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<AutoTransferListRes> getAutoTransfers(
		@RequestParam(required = false) AutoTransferStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<AutoTransfer> transfers;

		if (status != null && startDate != null && endDate != null) {
			transfers = autoTransferAdminService.getAutoTransfersByStatusAndDateRange(status, startDate, endDate, pageable);
		} else if (status != null) {
			transfers = autoTransferAdminService.getAutoTransfersByStatus(status, pageable);
		} else if (startDate != null && endDate != null) {
			transfers = autoTransferAdminService.getAutoTransfersByDateRange(startDate, endDate, pageable);
		} else {
			transfers = autoTransferAdminService.getAllAutoTransfers(pageable);
		}

		return PageResponse.of(transfers.map(AutoTransferListRes::from));
	}


	/**
     * 특정 자동이체 수동 실행
     * <p>
     * 관리자가 특정 자동이체를 즉시 실행합니다.
     * </p>
     *
     * @param autoTransferId 실행할 자동이체 ID
     */
    @PostMapping("/{autoTransferId}/execute")
    @ResponseStatus(HttpStatus.OK)
    public void executeAutoTransfer(
            @PathVariable Long autoTransferId
    ) {
        log.info("[관리자 자동이체 수동 실행] autoTransferId: {}", autoTransferId);
        autoTransferAdminService.executeAutoTransferManually(autoTransferId);
    }
}
