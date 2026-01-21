package com.example.BeGroom.notification.service.network;

import java.util.Map;

public interface NotificationNetworkService {
    // 프로토콜 연결 메서드
    default Object connect(Long memberId){ return new Object(); };

    // 알림 전송 메서드
    void send(Map<String, Object> msg, NotificationTarget target);

    // 네트워크 연결 종료 메서드
    default void disconnect(Long memberId) {}
}
