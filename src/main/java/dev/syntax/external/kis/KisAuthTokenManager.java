package dev.syntax.external.kis;

import dev.syntax.external.kis.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisAuthTokenManager {

    private final KisApiConfig config;
    private final WebClient kisClient;

    private String accessToken;
    private LocalDateTime expiresAt;

    public synchronized String getAccessToken() {
        if (accessToken == null || LocalDateTime.now().isAfter(expiresAt)) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        TokenResponse response = kisClient.post()
                .uri("/oauth2/tokenP")
                .header("Content-Type", "application/json; charset=utf-8")
                .bodyValue(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", config.getAppKey(),
                        "appsecret", config.getAppSecret()
                ))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        this.accessToken = response.getAccessToken();
        this.expiresAt = LocalDateTime.now().plusSeconds(response.getExpiresIn());

        System.out.println("[KIS] Access Token refreshed. New expiry: " + response.getAccessTokenExpiredAt());
    }
}
