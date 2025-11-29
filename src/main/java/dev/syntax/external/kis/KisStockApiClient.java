package dev.syntax.external.kis;

import dev.syntax.external.kis.dto.MultiPriceRes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KisStockApiClient {

    private final WebClient kisClient;
    private final KisAuthFilter auth;

    private static final String URL = "/uapi/domestic-stock/v1/quotations/intstock-multprice";
    private static final String TR_ID = "FHKST11300006";

    private static final List<String> DEFAULT_CODES = List.of(
            "005930", // 삼성전자
            "005380", // 현대차
            "035420", // NAVER
            "051910", // LG화학
            "000270", // 기아
            "068270", // 셀트리온
            "105560", // KB금융
            "055550", // 신한지주
            "003550", // LG
            "034730", // SK
            "035720" // 카카오

    );

    public MultiPriceRes getPrices(List<String> codes) {

        WebClient client = kisClient
                .mutate()
                .filter(auth.applyAuth(TR_ID))
                .build();

        return client.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(URL);
                    for (int i = 0; i < codes.size(); i++) {
                        int n = i + 1;
                        uriBuilder.queryParam("FID_INPUT_ISCD_" + n, codes.get(i));
                        uriBuilder.queryParam("FID_COND_MRKT_DIV_CODE_" + n, "J");
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(MultiPriceRes.class)
                .block();
    }

    /** 전체(30개) 종목 조회 */
    public MultiPriceRes getMultiPrice() {
        return getPrices(DEFAULT_CODES);
    }

}