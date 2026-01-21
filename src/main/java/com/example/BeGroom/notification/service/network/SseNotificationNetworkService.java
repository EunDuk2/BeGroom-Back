package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationNetworkService implements NotificationNetworkService {

    private final EmitterRepository emitterRepository;

    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @Override
    public SseEmitter connect(Long memberId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
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

        sendBySse(emitter, emitterId, "connect", "connected!");

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
        // 1. 해당 회원의 모든 Emitter를 찾습니다.
        Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(String.valueOf(memberId));

        // 2. 루프를 돌면서 정리합니다.
        emitters.forEach((id, emitter) -> {
            try {
                // [중요] 클라이언트에게 "연결 끝났다"고 기술적으로 끊어줍니다.
                emitter.complete();
            } catch (Exception e) {
                // 이미 끊긴 경우 등 에러 무시 (로그만 남김)
                log.warn("disconnect fail : " + id);
            } finally {
                // [중요] 저장소에서도 지워줍니다.
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
            Map<String, SseEmitter> emitters = emitterRepository.findAllStartWithById(String.valueOf(receiverId));

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
            System.out.print("SSE 연결이 끊겨서 전송 실패. Emitter 삭제함: userId={}");
            System.out.println(id);

        }
    }

}
