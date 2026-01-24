package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.notification.repository.EmitterRepository;
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

import static com.example.BeGroom.notification.domain.NotificationMessage.SSE_CONNECTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationNetworkService implements NotificationNetworkService {

    private final EmitterRepository emitterRepository;

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
            log.info("SSE Connection Timed Out: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });

        emitterRepository.save(emitterId, emitter);

        sendBySse(emitter, emitterId, "connect", SSE_CONNECTED);

        return emitter;
    }

    @Override
    public void send(Map<String, Object> msg, NotificationTarget target) {
        switch (target) {
            case NotificationTarget.Broadcast b -> sendToAll(msg);
            case NotificationTarget.Specific s -> sendToMembers(msg, s.memberIds());
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

    public void sendToAll(Map<String, Object> msg){
        Map<String, SseEmitter> allEmitters = emitterRepository.findAll();

        allEmitters.forEach((id, emitter) -> {
            sendBySse(emitter, id, "notification", msg);
        });
    }

    public void sendToMembers(Map<String, Object> msg, List<Long> receiverIds){
        for (Long receiverId : receiverIds) {
            Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(receiverId);
            emitters.forEach((id, emitter) -> {
                sendBySse(emitter, id, "notification", msg);
            });
        }
    }

    private void sendBySse(SseEmitter emitter, String id, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            emitterRepository.deleteById(id);
        }
    }

}
