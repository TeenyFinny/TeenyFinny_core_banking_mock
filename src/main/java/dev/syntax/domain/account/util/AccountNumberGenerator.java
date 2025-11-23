package dev.syntax.domain.account.util;

import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 계좌번호 생성 유틸리티
 * <p>
 * 무작위로 계좌번호를 생성합니다.
 * 형식: XXXX-XXX-XXXXXX (4자리-3자리-6자리)
 * 중복 방지를 위해 내부 Set에서 계좌번호 중복 여부를 확인합니다.
 * </p>
 *
 * @author TeenyFinny Core Banking Team
 */
public class AccountNumberGenerator {
    private static final Random RANDOM = new Random();
    private static final Set<String> ISSUED_NUMBERS = new HashSet<>();
    private static final int MAX_RETRY = 5;

    /**
     * 계좌번호를 생성합니다.
     *
     * @return "1234-567-890123" 형태의 계좌번호
     */
    public static synchronized String generate() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String account = generateRandom();

            // 중복 체크
            if (!ISSUED_NUMBERS.contains(account)) {
                ISSUED_NUMBERS.add(account);
                return account;
            }
        }

        throw new BusinessException(ErrorBaseCode.ACCOUNT_GENERATION_FAIL);
    }

    private static String generateRandom() {
        int part1 = 1000 + RANDOM.nextInt(9000);     // 1000~9999
        int part2 = 100 + RANDOM.nextInt(900);       // 100~999
        int part3 = 100000 + RANDOM.nextInt(900000); // 100000~999999

        return part1 + "-" + part2 + "-" + part3;
    }
}
