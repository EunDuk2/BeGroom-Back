package com.example.BeGroom.notification.util;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    private MessageUtil() {}

    public static Map<String, Object> createMessageByHashMap(String message) {
        if(message.isEmpty()){
            throw new IllegalArgumentException("메시지가 비어있습니다.");
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", message);
        return eventData;
    }

    public static String makeEmitterId(Long memberId, LocalDateTime connectTime) {
        try {
            long timestamp = connectTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return memberId + "_" + timestamp;
        } catch (Exception e) {
            return "";
        }
    }
}
