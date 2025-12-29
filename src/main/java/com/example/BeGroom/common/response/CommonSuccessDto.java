package com.example.BeGroom.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Schema(description = "공통 성공 응답")
public class CommonSuccessDto<T> {

    @Schema(
            description = "실제 응답 데이터",
            nullable = true
    )
    private T result;

    @Schema(
            description = "HTTP 상태 코드",
            example = "200"
    )
    private int statusCode;

    @Schema(
            description = "응답 메시지",
            example = "요청이 성공적으로 처리되었습니다."
    )
    private String message;

    public static <T> CommonSuccessDto<T> of(T result, HttpStatus status, String message) {
        return new CommonSuccessDto<>(
                result,
                status.value(),
                message
        );
    }
}
