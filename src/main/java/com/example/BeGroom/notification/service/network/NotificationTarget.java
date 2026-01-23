package com.example.BeGroom.notification.service.network;

import java.util.List;

public sealed interface NotificationTarget permits
        NotificationTarget.Specific,
        NotificationTarget.Broadcast {

    /** 특정 회원들 (N명) */
    record Specific(List<Long> memberIds) implements NotificationTarget {
        public static Specific of(List<Long> receiverIds) {
            return new Specific(receiverIds);
        }
    }

    /** 전체 회원 (Broadcast) */
    record Broadcast() implements NotificationTarget {
        public static final Broadcast INSTANCE = new Broadcast();
    }
}