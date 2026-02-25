package com.example.BeGroom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SseEventMessage {
    CONNECT("connect", "연결되었습니다."),
    COMMON_RECEIVE_NOTIFICATION_SUCCESS("notification", "새 알림이 도착했습니다!"),
    RETRY_RECEIVE_NOTIFICATION_SUCCESS("recovered-notification", "%d개의 새 알림이 도착했습니다!"),
    FIRST_CONNECT_UNREAD("unread-notification", "읽지 않은 알림이 %d개 있습니다!"),
    HEARTBEAT("heartbeat", "");

    private final String eventName;
    private final String messageTemplate;

    public String format(Object... args) {
        return String.format(this.messageTemplate, args);
    }
}
