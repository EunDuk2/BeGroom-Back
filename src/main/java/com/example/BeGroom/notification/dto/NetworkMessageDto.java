package com.example.BeGroom.notification.dto;

import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.util.MessageUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.BeGroom.notification.domain.SseEventMessage.COMMON_RECEIVE_NOTIFICATION_SUCCESS;

@Getter
@NoArgsConstructor
public class NetworkMessageDto {
    private Long receiverId;
    private String eventId;
    private String eventName;
    private String message;
    private Object data;

    public static NetworkMessageDto of(MemberNotification entity) {
        return NetworkMessageDto.builder()
                .receiverId(entity.getMember().getId())
                .eventId(String.valueOf(entity.getId()))
                .eventName("notification")
                .data(MessageUtil.createMessageByHashMap(COMMON_RECEIVE_NOTIFICATION_SUCCESS.getMessageTemplate()))
                .build();
    }

    @Builder
    private NetworkMessageDto(Long receiverId, String eventId, String eventName, String message, Object data) {
        this.receiverId = receiverId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.message = message;
        this.data = data;
    }
}