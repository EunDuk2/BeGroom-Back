package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.notification.dto.NetworkMessageDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface NotificationNetworkService {
    // 프로토콜 연결 메서드
    default Object connect(Long memberId, LocalDateTime connectTime){ return new Object(); }

    // 알림 전송 메서드
    void send(List<NetworkMessageDto> messages);

    // 네트워크 연결 종료 메서드
    default void disconnect(Long memberId) {}
}
