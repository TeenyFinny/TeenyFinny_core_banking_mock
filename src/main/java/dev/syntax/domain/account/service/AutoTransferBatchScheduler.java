package dev.syntax.domain.account.service;

import dev.syntax.domain.account.entity.AutoTransfer;
import dev.syntax.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 자동이체 배치 처리 스케줄러
 * <p>
 * 다중 인스턴스 환경에서도 동시 실행되지 않도록
 * ShedLock을 사용해 분산 락을 적용했습니다.
 * <p>
 * 하루 한 번 실행되는 작업 → 분산 락 필수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoTransferBatchScheduler {

    private final AutoTransferService autoTransferService;

    /**
     * 매일 새벽 1시에 실행되는 자동이체 배치 처리
     * ShedLock 적용으로 단일 인스턴스만 실행될 수 있음.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = "AutoTransferBatchScheduler_processScheduledAutoTransfers",
            lockAtLeastFor = "2m",  // 최소 잠금 유지 시간
            lockAtMostFor = "5m"    // 최대 잠금 유지 시간(장애 대비)
    )
    public void processScheduledAutoTransfers() {

        LocalDate today = LocalDate.now();
        log.info("[배치 시작] 자동이체 실행일 = {}", today);

        List<AutoTransfer> transfersToProcess = autoTransferService.findTransfersByDate(today);

        if (transfersToProcess.isEmpty()) {
            log.info("[배치 종료] 오늘 실행할 자동이체 없음.");
            return;
        }

        log.info("실행 대상 자동이체 수: {}", transfersToProcess.size());

        int successCount = 0;
        int failCount = 0;

        for (AutoTransfer transfer : transfersToProcess) {

            try {
                log.debug("자동이체 실행 시작: ID = {}, 출금계좌 = {}, 입금계좌 = {}, 금액 = {}",
                        transfer.getId(),
                        transfer.getFromAccount().getId(),
                        transfer.getToAccount().getId(),
                        transfer.getAmount()
                );

                autoTransferService.execute(transfer);
                successCount++;

            } catch (BusinessException e) {
                failCount++;

                String reason = e.getErrorCode().getMessage();
                StackTraceElement origin = e.getStackTrace()[0];

                log.warn("자동이체 실패: ID={} | 사유={} | 발생 위치={}:{}",
                        transfer.getId(),
                        reason,
                        origin.getClassName(),
                        origin.getLineNumber()
                );
            }
        }

        log.info("[배치 종료] 전체 = {}, 성공 = {}, 실패 = {}",
                transfersToProcess.size(), successCount, failCount);
    }
}
