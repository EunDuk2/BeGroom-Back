package com.example.BeGroom.notification.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.CreateNotificationResDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.example.BeGroom.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관련 API")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "회원 알림 조회", description = "회원의 알림 리스트를 불러온다.")
    public ResponseEntity<CommonSuccessDto<GetMemberNotificationResDto>> getAllMemberNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("현재 로그인된 객체 타입: " + principal.getClass().getName());
        System.out.println("현재 로그인된 객체 값: " + principal);

        if(userPrincipal == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = userPrincipal.getMemberId();
        GetMemberNotificationResDto response = notificationService.getMyNotifications(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(
                CommonSuccessDto.of(
                        response,
                        HttpStatus.CREATED,
                        "알림 조회 성공"
                )
        );
    }

    @PostMapping
    @Operation(summary = "알림 생성", description = "알림을 생성한다.")
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
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리한다.")
    public ResponseEntity<CommonSuccessDto<Boolean>> updateNotificationRead(@PathVariable Long mappingId) {
        notificationService.readNotification(mappingId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                true,
                                HttpStatus.CREATED,
                                "알림 읽음 성공"
                        )
                );
    }
}