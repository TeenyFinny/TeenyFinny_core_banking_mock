package dev.syntax.external.kis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class KisWebClient {

    private final KisApiConfig config;

    @Bean
    public WebClient kisClient() {
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
}
