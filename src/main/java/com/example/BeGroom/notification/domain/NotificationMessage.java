package com.example.BeGroom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationMessage {
    SSE_CONNECTED("SSE CONNECTED"),
    NEW_NOTIFICATION("새로운 알림이 도착했습니다!");

    private final String message;
}
