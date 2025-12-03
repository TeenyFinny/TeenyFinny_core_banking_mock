package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.domain.account.enums.AutoTransferStatus;
import dev.syntax.domain.account.repository.AutoTransferRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 관리자용 자동이체 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoTransferAdminServiceImpl implements AutoTransferAdminService {

    private final AutoTransferRepository autoTransferRepository;
    private final AutoTransferService autoTransferService;

    @Override
    public Page<AutoTransfer> getAllAutoTransfers(Pageable pageable) {
        return autoTransferRepository.findAll(pageable);
    }

    @Override
    public Page<AutoTransfer> getAutoTransfersByStatus(AutoTransferStatus status, Pageable pageable) {
        return autoTransferRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<AutoTransfer> getAutoTransfersByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return autoTransferRepository.findByNextTransferDayBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<AutoTransfer> getAutoTransfersByStatusAndDateRange(
            AutoTransferStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return autoTransferRepository.findByStatusAndNextTransferDayBetween(status, startDate, endDate, pageable);
    }

    @Override
    @Transactional
    public void executeAutoTransferManually(Long autoTransferId) {
        AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        log.info("[관리자 서비스] 자동이체 수동 실행 요청 - ID: {}", autoTransferId);
        
        // 기존 서비스의 실행 로직 재사용 (비즈니스 로직 일관성 유지)
        autoTransferService.execute(autoTransfer);
    }
}
