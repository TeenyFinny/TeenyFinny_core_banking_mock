package dev.syntax.external.kis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KisAuthFilter {

    private final KisAuthTokenManager tokenManager;
    private final KisApiConfig config;

    public ExchangeFilterFunction applyAuth(String trId) {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {

            ClientRequest newReq = ClientRequest.from(request)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + tokenManager.getAccessToken())
                    .header("appkey", config.getAppKey())
                    .header("appsecret", config.getAppSecret())
                    .header("tr_id", trId)
                    .header("custtype", "P")
                    .build();

            // 🔥 WebFlux는 반드시 Mono<ClientRequest> 반환해야 함
            return Mono.just(newReq);
        });
    }
}