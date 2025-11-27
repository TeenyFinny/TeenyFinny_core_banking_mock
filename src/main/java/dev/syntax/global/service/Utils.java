package dev.syntax.global.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 공통적으로 사용되는 서비스 로직은 이곳에 작성해주세요.
 */
public class Utils {

    /**
     * 정수(int) 값을 천 단위 구분 기호(,)가 포함된 문자열로 변환합니다.
     * <p>
     * 예) 230010000 → "230,010,000"
     * </p>
     *
     * <p><b>주의:</b> {@link String#format(String, Object...)}는 기본 Locale의
     * 숫자 형식 규칙을 따릅니다. 일관된 결과가 필요하면
     * {@code String.format(Locale.US, "%,d", num)}처럼 Locale을 명시하세요.</p>
     *
     * @param num 천 단위 구분 기호로 포맷할 정수 값
     * @return 천 단위 구분 기호가 포함된 문자열
     */
    public static String NumberFormattingService(int num) {
        return String.format("%,d", num);
    }

    /**
     * 정수(long) 값을 천 단위 구분 기호(,)가 포함된 문자열로 변환합니다.
     * <p>
     * 예) 230010000 → "230,010,000"
     * </p>
     *
     * <p><b>주의:</b> {@link String#format(String, Object...)}는 기본 Locale의
     * 숫자 형식 규칙을 따릅니다. 일관된 결과가 필요하면
     * {@code String.format(Locale.US, "%,d", num)}처럼 Locale을 명시하세요.</p>
     *
     * @param num 천 단위 구분 기호로 포맷할 정수 값
     * @return 천 단위 구분 기호가 포함된 문자열
     */
    public static String NumberFormattingService(long num) {
        return String.format("%,d", num);
    }


    /**
     * BigDecimal 값을 천 단위 구분 기호(,)가 포함된 문자열로 변환합니다.
     * <p>
     * 예) 230010000.00 → "230,010,000"
     * </p>
     *
     * <p><b>주의:</b> 소수점 이하는 제거되고 정수 부분만 표시됩니다.
     * 소수점을 포함하려면 {@code String.format("%,.2f", num.doubleValue())}를 사용하세요.</p>
     *
     * @param num 천 단위 구분 기호로 포맷할 BigDecimal 값
     * @return 천 단위 구분 기호가 포함된 문자열 (소수점 제거)
     */
    public static String NumberFormattingService(BigDecimal num) {
        if (num == null) {
            return "0";
        }
        return new DecimalFormat("#,###").format(num);
    }

    /**
     * 실수 값을 소수점 둘째 자리까지 반올림하여 문자열로 변환합니다.
     *
     * <p>
     * 예) <br>
     *  - 0.12345  → "0.12"<br>
     *  - 1.567    → "1.57"<br>
     *  - 12.0     → "12.00"
     * </p>
     *
     * <p>
     * 내부적으로 {@link java.math.BigDecimal}을 사용하여 반올림 오차를 방지하며,
     * 정확한 소수점 자리수 제어가 가능합니다.
     * </p>
     *
     * @param value 포맷할 실수 값 (null이면 "0.00" 반환)
     * @return 소수점 둘째 자리까지 반올림된 문자열
     */
    public static String FormatToTwoDecimal(Double value) {
        if (value == null) {
            return "0.00";
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    /**
     * Double 값을 소수점 둘째 자리까지 반올림하여 double로 반환합니다.
     *
     * <p>예:</p>
     * <ul>
     *     <li>12.345 → 12.35</li>
     *     <li>12.344 → 12.34</li>
     *     <li>null → 0.00</li>
     * </ul>
     *
     * @param value 반올림할 Double 값
     * @return 소수점 둘째 자리까지 반올림된 double 값
     */
    public static double RoundToTwoDecimal(Double value) {
        if (value == null) {
            return 0.00;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
