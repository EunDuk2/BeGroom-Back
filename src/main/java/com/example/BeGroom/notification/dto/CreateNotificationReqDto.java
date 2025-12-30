package com.example.BeGroom.notification.dto;

import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationReqDto {
    @NotNull
    @Schema(example = "NOTICE")
    private NotificationType type;

    @NotEmpty
    @Schema(example = "서버 공지 알림")
    private String title;

    @NotEmpty
    @Schema(example = "13:00부터 점검이 시작됩니다.")
    private String message;

    @NotEmpty
    @Schema(example = "/")
    private String link;
}
