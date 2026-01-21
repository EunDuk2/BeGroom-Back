package com.example.BeGroom.notification.util;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    private MessageUtil() {}

    public static Map<String, Object> createSseMessage(String message) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", message);
        return eventData;
    }
}
