package dev.syntax.external.kis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("access_token_token_expired")
    private String accessTokenExpiredAt;  // yyyy-MM-dd HH:mm:ss
}
