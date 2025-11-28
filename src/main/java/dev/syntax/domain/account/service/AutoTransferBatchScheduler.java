package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.AutoTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 자동이체 배치 처리 스케줄러
 * <p>
 * 매일 등록된 자동이체 중 실행일이 도래한 자동이체를 자동으로 실행합니다.
 * 기존 엔티티나 서비스에 영향을 주지 않고 독립적으로 동작합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoTransferBatchScheduler {

    private final AutoTransferService autoTransferService;

    /**
     * 매일 새벽 1시에 실행되는 자동이체 배치 처리
     * <p>
     * 오늘 실행일이 도래한 모든 자동이체를 조회하여 실행합니다.
     * 각 자동이체는 독립적으로 처리되며, 하나의 실패가 다른 자동이체 실행에 영향을 주지 않습니다.
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void processScheduledAutoTransfers() {
        LocalDate today = LocalDate.now();
        log.info("자동이체 배치 처리 시작: 실행일 = {}", today);

        try {
            // 오늘 실행해야 하는 모든 자동이체 조회
            List<AutoTransfer> transfersToProcess = autoTransferService.findTransfersByDate(today);

            if (transfersToProcess.isEmpty()) {
                log.info("오늘 실행할 자동이체가 없습니다.");
                return;
            }

            log.info("실행 대상 자동이체 수: {}", transfersToProcess.size());

            int successCount = 0;
            int failCount = 0;

            // 각 자동이체를 순차적으로 실행
            for (AutoTransfer transfer : transfersToProcess) {
                try {
                    log.debug("자동이체 실행 시작: ID = {}, 출금계좌 = {}, 입금계좌 = {}, 금액 = {}",
                            transfer.getId(),
                            transfer.getFromAccount().getId(),
                            transfer.getToAccount().getId(),
                            transfer.getAmount());

                    autoTransferService.execute(transfer);
                    successCount++;

                    log.debug("자동이체 실행 성공: ID = {}", transfer.getId());

                } catch (Exception e) {
                    failCount++;
                    log.error("자동이체 실행 실패: ID = {}, 오류 = {}", transfer.getId(), e.getMessage(), e);
                    // 개별 자동이체 실패는 로깅만 하고 계속 진행
                }
            }

            log.info("자동이체 배치 처리 완료: 전체 = {}, 성공 = {}, 실패 = {}",
                    transfersToProcess.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("자동이체 배치 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

