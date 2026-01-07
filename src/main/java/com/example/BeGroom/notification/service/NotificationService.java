package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    public void send(List<Long> receiverIds, Long templateId, Map<String, String> variables);
    public GetMemberNotificationResDto getMyNotifications(Long memberId);

    public void readAllNotifications(Long memberId);
    public void readNotification(Long mappingId);
    Notification createNotification(CreateNotificationReqDto reqDto);
    public void sendToAllMembers(Long templateId, Map<String, String> variables);

    public SseEmitter subscribe(Long memberId);
}
