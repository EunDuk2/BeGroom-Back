package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationHistoryService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    /** 알림 내역 생성 */
    public List<MemberNotification> createMemberNotification(List<Long> receiverIds, Long templateId, Map<String, String> variables) {

        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 알림 템플릿이 없습니다."));

        String jsonMetaData = convertToJson(variables);

        List<Member> receivers = memberRepository.findAllById(receiverIds);
        if (receivers.size() != receiverIds.size()) {
            throw new EntityNotFoundException("해당하는 멤버가 없습니다.");
        }


        return receivers.stream()
                .map(receiver -> new MemberNotification(receiver, template, jsonMetaData))
                .toList();
    }

    /** Map to JSON type */
    private String convertToJson(Map<String, String> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
