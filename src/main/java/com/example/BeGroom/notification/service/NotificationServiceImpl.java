package com.example.BeGroom.notification.service;

import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.event.NotificationSavedEvent;
import com.example.BeGroom.notification.repository.MemberNotificationJdbcRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import com.example.BeGroom.notification.util.MessageUtil;
import com.google.common.collect.Lists;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.BeGroom.notification.domain.SseEventMessage.COMMON_RECEIVE_NOTIFICATION_SUCCESS;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final ApplicationEventPublisher eventPublisher;

    private final NotificationRepository notificationRepository;
    private final MemberNotificationRepository memberNotificationRepository;
    private final MemberRepository memberRepository;
    private final MemberNotificationJdbcRepository memberNotificationJdbcRepository;
    private final NotificationNetworkService notificationNetworkService;
    private final NotificationHistoryService notificationHistoryService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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
        memberNotificationJdbcRepository.batchInsert(notifications);

//        // Network message 생성
//        List<NetworkMessageDto> eventData = notifications.stream()
//                .map(NetworkMessageDto::of)
//                .collect(Collectors.toList());
//
//        // 커밋이 된 뒤에 SSE event 수행
//        eventPublisher.publishEvent(new NotificationSavedEvent(eventData));
    }

    @Transactional(readOnly = false)
    public void sendToAllMembers(Long templateId, Map<String, String> variables) {
        List<Object[]> result = memberRepository.findMinMaxId();
        if (result.isEmpty() || result.get(0)[0] == null) {
            return;
        }
        Object[] row = result.getFirst();
        long minId = (Long) row[0];
        long maxId = (Long) row[1];

        //1. 파티션 사이즈
        int partitionSize = 20000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (long start = minId; start <= maxId; start += partitionSize) {
            long end = Math.min(start + partitionSize - 1, maxId);
            final long currentStart = start;
            final long currentEnd = end;

            System.out.println("start: " + start + " end: " + end);
            futures.add(CompletableFuture.runAsync(() -> {
                LocalDateTime now = LocalDateTime.now().minusSeconds(1);
                System.out.println("Debug cuurent time: " + now);
                memberNotificationJdbcRepository.partitionInsert(templateId, variables, currentStart, currentEnd); // DB 작성
                List<NetworkMessageDto> chunkData =
                        memberNotificationJdbcRepository.findNetworkMessageDtoByRange(templateId, currentStart, currentEnd, now); // 실시간 메시지 객체 생성
                if (!chunkData.isEmpty()) {
                    eventPublisher.publishEvent(new NotificationSavedEvent(chunkData)); // 실시간 메시지 전송 이벤트 발송
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Override
    @Transactional
    public void readNotification(Long mappingId) {
        MemberNotification memberNotification = memberNotificationRepository.findById(mappingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 알림이 존재하지 않습니다."));

        memberNotification.read();
    }

    @Transactional
    public void readAllNotifications(Long memberId) {
        memberNotificationRepository.bulkMarkAsRead(memberId);
    }
}
