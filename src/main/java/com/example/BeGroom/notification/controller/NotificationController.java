package com.example.BeGroom.notification.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationTemplate;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.CreateNotificationResDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.service.NotificationService;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관련 API")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationNetworkService notificationNetworkService;

    /// TODO: PC코드에 swagger등 핵심 비즈니스 로직이 아닌 코드들이 있으면 코드가 비대해지거나 설명이 비대졌을떄 보기가 편할까요?
    @GetMapping
    @Operation(summary = "사용자 알림 조회", description = "특정 사용자의 알림 리스트를 불러온다.")
    public ResponseEntity<CommonSuccessDto<GetMemberNotificationResDto>> getAllMemberNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal){
        if(userPrincipal == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = userPrincipal.getMemberId();
        GetMemberNotificationResDto response = notificationService.getMyNotifications(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(
                CommonSuccessDto.of(
                        response,
                        HttpStatus.OK,
                        "알림 조회 성공"
                )
        );
    }

    @PostMapping
    @Operation(summary = "알림 생성", description = "알림 템플릿을 생성한다.")
    public ResponseEntity<CommonSuccessDto<CreateNotificationResDto>> create(
            @Valid @RequestBody CreateNotificationReqDto reqDto
    ) {
        Notification notification = notificationService.createNotification(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new CreateNotificationResDto(notification.getId()),
                                HttpStatus.CREATED,
                                "알림 생성 성공"
                        )
                );
    }

    @PatchMapping("/{mappingId}")
    @Operation(summary = "알림 읽음 처리", description = "사용자의 알림을 읽음 처리한다.")
    public ResponseEntity<CommonSuccessDto<Boolean>> updateNotificationRead(@PathVariable Long mappingId) {
        notificationService.readNotification(mappingId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                true,
                                HttpStatus.OK,
                                "알림 읽음 성공"
                        )
                );
    }

    @PatchMapping("/all")
    @Operation(summary = "알림 전체 읽음 처리", description = "사용자의 알림을 전체 읽음 처리한다.")
    public ResponseEntity<CommonSuccessDto<Boolean>> updateAllNotificationRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        notificationService.readAllNotifications(userPrincipal.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                true,
                                HttpStatus.OK,
                                "알림 전체 읽음 성공"
                        )
                );
    }


    @PostMapping("/send/inspect")
    @Operation(summary = "관리자 서비스 점검 알림 송신", description = "관리자가 사용자들에게 점검 알림을 보낸다.")
    public ResponseEntity<CommonSuccessDto<Boolean>> sendInspectNotiByAdmin(
            @RequestBody Map<String, String> requestMap
    ) {
        notificationService.sendToAllMembers(NotificationTemplate.NOTICE_SERVICE_INSPECTION.getId(), requestMap);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                true,
                                HttpStatus.OK,
                                "서비스 점검 알림 전송 성공"
                        )
                );
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 구독 (SSE 연결)", description = "SSE와 연결합니다.")
        public Object subscribe(@AuthenticationPrincipal UserPrincipal userPrincipal) {

        return notificationNetworkService.connect(userPrincipal.getMemberId(), LocalDateTime.now());
    }
}