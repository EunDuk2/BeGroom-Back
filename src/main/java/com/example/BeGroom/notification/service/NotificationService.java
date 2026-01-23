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
    /** 알림 Create, Read */
    Notification createNotification(CreateNotificationReqDto reqDto);
    public GetMemberNotificationResDto getMyNotifications(Long memberId);

    /** 알림 읽음 처리 로직 */
    public void readNotification(Long mappingId);
    public void readAllNotifications(Long memberId);

    /** 실시간 메시지 전송 로직 */
    public void send(List<Long> receiverIds, Long templateId, Map<String, String> variables);
    public void sendToAllMembers(Long templateId, Map<String, String> variables);
}
