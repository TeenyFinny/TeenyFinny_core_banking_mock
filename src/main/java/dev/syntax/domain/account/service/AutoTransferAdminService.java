package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * 관리자용 자동이체 서비스 인터페이스
 * <p>
 * 관리자 전용 자동이체 조회 및 실행 기능을 제공합니다.
 * 기존 AutoTransferService와 분리하여 관리자 기능의 독립성을 보장합니다.
 * </p>
 */
public interface AutoTransferAdminService {

    /**
     * 전체 자동이체 조회 (페이징)
     */
    Page<AutoTransfer> getAllAutoTransfers(Pageable pageable);

    /**
     * 상태별 자동이체 조회 (페이징)
     */
    Page<AutoTransfer> getAutoTransfersByStatus(AutoTransferStatus status, Pageable pageable);

    /**
     * 날짜 범위별 자동이체 조회 (페이징)
     */
    Page<AutoTransfer> getAutoTransfersByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 상태 및 날짜 범위별 자동이체 조회 (페이징)
     */
    Page<AutoTransfer> getAutoTransfersByStatusAndDateRange(
            AutoTransferStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * 특정 자동이체 수동 실행
     */
    void executeAutoTransferManually(Long autoTransferId);
}
