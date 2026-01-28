package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.dto.NotificationSendResult;
import com.example.BeGroom.notification.dto.SseMessageDto;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static com.example.BeGroom.notification.domain.SseEventMessage.*;
import static com.example.BeGroom.notification.util.MessageUtil.makeEmitterId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationNetworkService implements NotificationNetworkService {

    private final EmitterRepository emitterRepository;
    private final MemberNotificationRepository memberNotificationRepository;

    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @Override
    public SseEmitter connect(Long memberId, LocalDateTime connectTime) {
        if(memberId == null || memberId <= 0L){
            throw new IllegalArgumentException("멤버 ID는 양수여야 합니다.");
        }

        long timestamp = connectTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String emitterId = memberId + "_" + timestamp;
        SseEmitter emitter = new SseEmitter(defaultTimeout);

        emitter.onCompletion(() -> {
            log.info("SSE Connection Completed: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            log.info("SSE Connection Timed Out: {}", emitterId);
        });
        emitter.onError((_) -> {
            emitter.complete();
        });

        emitterRepository.save(emitterId, emitter);

        return emitter;
    }

    @Override
    public void send(List<NetworkMessageDto> messages) {
        for (NetworkMessageDto message : messages) {

            Long receiverId = message.getReceiverId();

            Map<String, SseEmitter> userEmitters = emitterRepository.findAllStartWithById(receiverId);

            userEmitters.forEach((emitterId, emitter) -> {
                sendBySse(
                        emitter,
                        emitterId,
                        message.getEventId(),
                        message.getEventName(),
                        message.getData()
                );
            });
        }
    }

    @Override
    public void disconnect(Long memberId) {
        if(memberId == null || memberId <= 0L){
            throw new IllegalArgumentException("멤버 ID는 양수여야 합니다.");
        }

        Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(memberId);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("[SSE] Disconnect failed : " + id);
            } finally {
                emitterRepository.deleteById(id);
            }
        });
    }

    public SseEmitter subscribeWithHistory(Long memberId, String lastEventId, LocalDateTime connectTime) {
        SseEmitter emitter = this.connect(memberId, connectTime);
        String emitterId = makeEmitterId(memberId, connectTime);
        this.sseConnectionMessage(memberId, lastEventId, emitterId, emitter);
        return emitter;
    }

    public NotificationSendResult sseConnectionMessage(Long memberId, String lastEventId, String emitterId, SseEmitter emitter) {
        if (!lastEventId.isEmpty()) {
            try {
                long lastId = Long.parseLong(lastEventId);
                Object[] table = memberNotificationRepository.findLostSummary(memberId, lastId);
                Object[] summary = (Object[]) table[0];

                long lostCount = ((Number) summary[0]).longValue();
                Long maxId = summary[1] != null ? ((Number) summary[1]).longValue() : 0L;

                if (lostCount > 0) {
                    sendBySse(emitter, emitterId, String.valueOf(maxId), "notification", RETRY_RECEIVE_NOTIFICATION_SUCCESS.format(lostCount));
                    return NotificationSendResult.success(emitterId, String.valueOf(maxId));
                }

                return NotificationSendResult.success(emitterId, "");
            } catch (Exception e) {
                log.warn("[SSE] Disconnect failed : " + lastEventId);
                return NotificationSendResult.failure(emitterId, "", e.getMessage());
            }
        } else {
            Object[] table = memberNotificationRepository.findUnreadSummary(memberId);
            Object[] summary = (Object[]) table[0];

            long unreadCount = ((Number) summary[0]).longValue();
            Long maxId = summary[1] != null ? ((Number) summary[1]).longValue() : 0L;

            if (unreadCount > 0) {
                sendBySse(emitter, emitterId, String.valueOf(maxId), "notification", FIRST_CONNECT_UNREAD.format(unreadCount));
                return NotificationSendResult.success(emitterId, String.valueOf(maxId));
            } else {
                sendHeartBeat(emitter, emitterId);
                return NotificationSendResult.success(emitterId, "");
            }
        }
    }

    public NotificationSendResult sendBySse(SseEmitter emitter, String emitterId, String eventId, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name(eventName)
                    .data(data));
            return NotificationSendResult.success(emitterId, eventId);
        } catch (IOException | IllegalStateException e) {
            emitterRepository.deleteById(emitterId);
            return NotificationSendResult.failure(emitterId, eventId, e.getMessage());
        }
    }

    public NotificationSendResult sendHeartBeat(SseEmitter emitter, String emitterId) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat"));
            return NotificationSendResult.success(emitterId, "");
        } catch (IOException | IllegalStateException e) {
            emitterRepository.deleteById(emitterId);
            return NotificationSendResult.failure(emitterId, "", e.getMessage());
        }
    }

}
