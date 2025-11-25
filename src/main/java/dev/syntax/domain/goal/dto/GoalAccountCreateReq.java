package dev.syntax.domain.goal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GoalAccountCreateReq(

        @NotBlank(message = "목표 이름을 입력해주세요.")
        String name

) {
}
