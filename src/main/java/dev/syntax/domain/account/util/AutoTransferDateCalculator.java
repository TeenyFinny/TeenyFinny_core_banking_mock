package dev.syntax.domain.account.util;

import java.time.LocalDate;

/**
 * 자동이체 다음 실행일 계산 유틸리티
 * <p>
 * 매달 지정된 날짜에 자동이체가 실행되도록 다음 실행일을 계산합니다.
 * 31일처럼 해당 달에 존재하지 않는 날짜는 마지막 날로 자동 조정됩니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
public class AutoTransferDateCalculator {

    /**
     * 다음 자동이체 실행일을 계산합니다.
     * <p>
     * 로직:
     * 1. 지정된 transferDay가 현재 달에 존재하지 않으면 마지막 날로 조정
     * (예: 31일 지정 시 2월은 28일 또는 29일)
     * 2. 해당 날짜가 오늘보다 이후면 현재 달의 해당 날짜 반환
     * 3. 해당 날짜가 오늘보다 이전이거나 같으면 다음 달의 해당 날짜 반환
     * </p>
     *
     * @param transferDay 매달 실행할 날짜 (1~31)
     * @return 다음 실행일 (LocalDate)
     * <p>
     * example
     * // 오늘이 2024-01-10이고 transferDay가 25일인 경우
     * getNextTransferDate(25); // returns 2024-01-25
     * <p>
     * // 오늘이 2024-01-26이고 transferDay가 25일인 경우
     * getNextTransferDate(25); // returns 2024-02-25
     * <p>
     * // 오늘이 2024-02-15이고 transferDay가 31일인 경우
     * getNextTransferDate(31); // returns 2024-02-29 (2024년은 윤년)
     */
    public static LocalDate getNextTransferDate(int transferDay) {
        LocalDate now = LocalDate.now();
        int lastDay = now.lengthOfMonth();

        // 이번 달 실행일이 존재하지 않으면 달의 마지막 날로 처리
        int runDay = Math.min(transferDay, lastDay);

        LocalDate thisMonth = now.withDayOfMonth(runDay);

        // 오늘 날짜보다 이전이면 다음 달
        if (!thisMonth.isAfter(now)) {
            LocalDate nextMonth = now.plusMonths(1);
            int nextLastDay = nextMonth.lengthOfMonth();
            int nextRunDay = Math.min(transferDay, nextLastDay);
            return nextMonth.withDayOfMonth(nextRunDay);
        }

        return thisMonth;
    }

    public static LocalDate calculateNextTransferDate(LocalDate currentNext, Integer newPayDay) {

        int lastDayOfMonth = currentNext.lengthOfMonth();
        int validDay = Math.min(newPayDay, lastDayOfMonth);

        return currentNext.withDayOfMonth(validDay);
    }
}
