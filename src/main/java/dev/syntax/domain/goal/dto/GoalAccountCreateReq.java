package dev.syntax.domain.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GoalAccountCreateReq(
        @NotNull
        Long childCoreId,
        @NotBlank(message = "목표 이름을 입력해주세요.")
        String name
) {
}
