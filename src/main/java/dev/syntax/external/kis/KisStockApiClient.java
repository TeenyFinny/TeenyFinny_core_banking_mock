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

    public MultiPriceResponse getMultiPrice(List<String> codes) {

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
                .bodyToMono(MultiPriceResponse.class)
                .block();
    }

}
