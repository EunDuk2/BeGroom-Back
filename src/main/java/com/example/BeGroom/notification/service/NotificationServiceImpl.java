package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberNotificationRepository memberNotificationRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public GetMemberNotificationResDto getMyNotifications(Long memberId) {
        List<MemberNotification> notiList = memberNotificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        long unreadCount = memberNotificationRepository.countByMemberIdAndIsReadFalse(memberId);
        return GetMemberNotificationResDto.of(notiList, unreadCount);
    }

    @Transactional(readOnly = false)
    @Override
    public void send(List<Long> receiverIds, Long templateId, Map<String, String> variables) {
        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 알림 템플릿이 없습니다."));

        String jsonMetaData;
        jsonMetaData = objectMapper.writeValueAsString(variables);

        List<Member> receivers = memberRepository.findAllById(receiverIds);

        List<MemberNotification> notifications = receivers.stream()
                .map(receiver -> new MemberNotification(receiver, template, jsonMetaData))
                .collect(Collectors.toList());

        memberNotificationRepository.saveAll(notifications);

        String notificationContent = "새로운 알림이 도착했습니다!"; // 실제론 템플릿 치환된 메시지 사용

        for (Long receiverId : receiverIds) {
            Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(String.valueOf(receiverId));

            emitters.forEach((id, emitter) -> {
                sendToClient(emitter, id, "notification", notificationContent);
            });
        }
    }

    @Transactional(readOnly = false)
    public void sendToAllMembers(Long templateId, Map<String, String> variables) {
        Notification template = notificationRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 알림 템플릿이 없습니다."));

        String jsonMetaData;
        jsonMetaData = objectMapper.writeValueAsString(variables);

        List<Long> targetIds = memberRepository.findAllIds();

        List<MemberNotification> notifications = targetIds.stream()
                .map(targetId -> {
                    Member memberProxy = memberRepository.getReferenceById(targetId);
                    return new MemberNotification(memberProxy, template, jsonMetaData);
                })
                .collect(Collectors.toList());

        memberNotificationRepository.saveAll(notifications);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "새로운 알림이 도착했습니다!");
        eventData.put("link", "https://begroom.vercel.app/my");

        Map<String, SseEmitter> allEmitters = emitterRepository.findAll();

        allEmitters.forEach((id, emitter) -> {
            sendToClient(emitter, id, "notification",eventData);
        });
    }

    @Transactional
    @Override
    public void readNotification(Long mappingId) {
        MemberNotification memberNoti = memberNotificationRepository.findById(mappingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));

        memberNoti.read();
    }

    @Transactional
    public void readAllNotifications(Long memberId) {
        memberNotificationRepository.bulkMarkAsRead(memberId);
    }

    @Override
    @Transactional
    public Notification createNotification(CreateNotificationReqDto reqDto) {

        Notification notification = Notification.createNotification(
                reqDto.getType(),
                reqDto.getTitle(),
                reqDto.getMessage(),
                reqDto.getLink()
        );
        notificationRepository.save(notification);

        return notification;
    }

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private final EmitterRepository emitterRepository;

    @Override
    public SseEmitter subscribe(Long memberId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitterRepository.save(emitterId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        Map<String, String> eventData = new HashMap<>();
        eventData.put("message", "SSE connected");

        sendToClient(emitter, emitterId, "connect", eventData);

        return emitter;
    }

    private void sendToClient(SseEmitter emitter, String id, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            emitterRepository.deleteById(id);
            System.out.print("SSE 연결이 끊겨서 전송 실패. Emitter 삭제함: userId={}");
            System.out.println(id);

        }
    }

}
