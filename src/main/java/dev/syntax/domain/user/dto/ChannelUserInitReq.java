package dev.syntax.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChannelUserInitReq(
	@NotNull
	Long channelUserId,

    @NotBlank
    String role,

	@NotBlank
	String name,

	@NotBlank
	String phoneNumber,

    @JsonFormat(pattern = "yyyy-MM-dd")
	@NotNull
	LocalDate birthDate
) {
}
