package dev.syntax.external.kis;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.external.kis.dto.TokenRes;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisAuthTokenManagerForDev {
    private static final String TOKEN_CACHE_FILE = "kis-token-cache.json";


    private final KisApiConfig config;
    private final WebClient kisClient;

    private String accessToken;
    private LocalDateTime expiresAt;

    @PostConstruct
    public void init() {
        loadTokenFromFile();

        if (accessToken != null && expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            log.info("[DEV] Cached token expired. Refreshing...");
            refreshToken();
            saveTokenToFile();
        }
    }


    public synchronized String getAccessToken() {
        if (accessToken == null || expiresAt == null || LocalDateTime.now().isAfter(expiresAt)) {
            refreshToken();
            saveTokenToFile();
        }
        return accessToken;
    }

    private void saveTokenToFile() {
        try (FileWriter fw = new FileWriter(TOKEN_CACHE_FILE)) {
            fw.write("""
        {
          "accessToken": "%s",
          "expiresAt": "%s"
        }
        """.formatted(accessToken, expiresAt.toString()));
        } catch (IOException e) {
            log.error("Failed to save KIS token", e);
        }
    }

    private void loadTokenFromFile() {
        try {
            File file = new File(TOKEN_CACHE_FILE);
            if (!file.exists()) return;

            String json = Files.readString(file.toPath());
            Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);

            this.accessToken = (String) map.get("accessToken");
            this.expiresAt = LocalDateTime.parse((String) map.get("expiresAt"));

            log.info("[DEV] Loaded cached KIS token from file");
        } catch (Exception e) {
            log.error("Failed to load cached KIS token", e);
        }
    }

    private void refreshToken() {
        TokenRes response = kisClient.post()
                .uri("/oauth2/tokenP")
                .header("Content-Type", "application/json; charset=utf-8")
                .bodyValue(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", config.getAppKey(),
                        "appsecret", config.getAppSecret()
                ))
                .retrieve()
                .bodyToMono(TokenRes.class)
                .block();

        this.accessToken = response.getAccessToken();
        this.expiresAt = LocalDateTime.now().plusSeconds(response.getExpiresIn());

        log.info("[KIS] Access Token refreshed. New expiry: " + response.getAccessTokenExpiredAt());
    }
}