package dev.syntax.external.kis;

import dev.syntax.external.kis.dto.MultiPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisStockApiClient {

    private final WebClient kisClient;
    private final KisAuthFilter auth;

    private static final String URL = "/uapi/domestic-stock/v1/quotations/intstock-multprice";
    private static final String TR_ID = "FHKST11300006";

    private static final List<String> DEFAULT_CODES = List.of(
            "005930", // 삼성전자
            "000660", // SK하이닉스
            "373220", // LG에너지솔루션
            "207940", // 삼성바이오로직스
            "005935", // 삼성전자우
            "005380", // 현대차
            "035420", // NAVER
            "051910", // LG화학
            "000270", // 기아
            "068270", // 셀트리온
            "028260", // 삼성물산
            "096770", // SK이노베이션
            "105560", // KB금융
            "055550", // 신한지주
            "066570", // LG전자
            "003550", // LG
            "012330", // 현대모비스
            "034730", // SK
            "017670", // SK텔레콤
            "015760", // 한국전력
            "032830", // 삼성생명
            "010130", // 고려아연
            "009150", // 삼성전기
            "086790", // 하나금융지주
            "003670", // 포스코퓨처엠
            "051900", // LG생활건강
            "035720", // 카카오
            "066970", // 엘앤에프
            "018260", // 삼성에스디에스
            "011200"  // HMM
    );

    public MultiPriceResponse getMultiPrice() {

        WebClient client = kisClient
                .mutate()
                .filter(auth.applyAuth(TR_ID))
                .build();

        return client.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(URL);
                    for (int i = 0; i < DEFAULT_CODES.size(); i++) {
                        int n = i + 1;
                        uriBuilder.queryParam("FID_INPUT_ISCD_" + n, DEFAULT_CODES.get(i));
                        uriBuilder.queryParam("FID_COND_MRKT_DIV_CODE_" + n, "J");
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(MultiPriceResponse.class)
                .block();
    }

}