package dev.syntax.domain.account.util;

import java.security.SecureRandom;

/**
 * 계좌번호 생성 유틸리티
 * <p>
 * 무작위로 계좌번호를 생성합니다.
 * 형식: XXXX-XXX-XXXXXX (4자리-3자리-6자리)
 * 중복 여부는 DB UNIQUE 제약에서 검증합니다.
 * </p>
 */
public class AccountNumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 계좌번호를 생성합니다.
     *
     * @return "1234-567-890123" 형태의 계좌번호
     */
    public static String generate() {
        int part1 = 1000 + RANDOM.nextInt(9000);     // 1000~9999
        int part2 = 100 + RANDOM.nextInt(900);       // 100~999
        int part3 = 100000 + RANDOM.nextInt(900000); // 100000~999999

        return part1 + "-" + part2 + "-" + part3;
    }
}
