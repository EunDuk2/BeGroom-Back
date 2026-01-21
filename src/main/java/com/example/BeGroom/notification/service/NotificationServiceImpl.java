package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import com.example.BeGroom.notification.service.network.NotificationTarget;
import com.example.BeGroom.notification.util.MessageUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final NotificationNetworkService notificationNetworkService;
    private final NotificationHistoryService notificationHistoryService;

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

    @Override
    @Transactional(readOnly = true)
    public GetMemberNotificationResDto getMyNotifications(Long memberId) {
        List<MemberNotification> notiList = memberNotificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        long unreadCount = memberNotificationRepository.countByMemberIdAndIsReadFalse(memberId);
        return GetMemberNotificationResDto.of(notiList, unreadCount);
    }

    @Override
    @Transactional(readOnly = false)
    public void send(List<Long> receiverIds, Long templateId, Map<String, String> variables) {
        // MemberNotification 객체 생성
        List<MemberNotification> notifications = notificationHistoryService.createMemberNotification(receiverIds, templateId, variables);

        // DB Insert
        memberNotificationRepository.saveAll(notifications);

        // 원하는 메시지 컨텐츠 생성
        Map<String, Object> eventData = MessageUtil.createSseMessage("새로운 알림이 도착했습니다!");

        // 실시간 메시지 전송
        notificationNetworkService.send(eventData, NotificationTarget.Specific.of(receiverIds));
    }

    @Transactional(readOnly = false)
    public void sendToAllMembers(Long templateId, Map<String, String> variables) {
        // MemberNotification 객체 생성
        List<Long> receiverIds = memberRepository.findAllIds();
        List<MemberNotification> notifications = notificationHistoryService.createMemberNotification(receiverIds, templateId, variables);

        // DB Insert
        memberNotificationRepository.saveAll(notifications);

        // 원하는 메시지 컨텐츠 생성
        Map<String, Object> eventData = MessageUtil.createSseMessage("새로운 알림이 도착했습니다!");

        // 실시간 메시지 전송
        notificationNetworkService.send(eventData, new NotificationTarget.Broadcast());
    }

    @Override
    @Transactional
    public void readNotification(Long mappingId) {
        MemberNotification memberNotification = memberNotificationRepository.findById(mappingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));

        memberNotification.read();
    }

    @Transactional
    public void readAllNotifications(Long memberId) {
        memberNotificationRepository.bulkMarkAsRead(memberId);
    }
}
