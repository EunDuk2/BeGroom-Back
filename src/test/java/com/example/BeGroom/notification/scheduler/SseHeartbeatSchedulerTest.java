package com.example.BeGroom.notification.scheduler;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.notification.domain.NotificationTemplate;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.service.NotificationService;
import com.example.BeGroom.notification.service.network.SseNotificationNetworkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SseHeartbeatSchedulerTest extends IntegrationTestSupport {
    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @MockitoSpyBean
    private SseNotificationNetworkService sseNotificationNetworkService;

    @Autowired
    private SseHeartbeatScheduler sseHeartbeatScheduler;

    @Autowired
    private EmitterRepository emitterRepository;

    @AfterEach
    void tearDown() {
        emitterRepository.deleteAll();
    }

    @Test
    @DisplayName("하트비트 스케줄러가 실행되면 모든 등록된 Emitter에게 SSE 하트비트 메시지를 보낸다.")
    void sendPeriodicNotifications() {
        // Given
        Map<String, SseEmitter> newEmitters = new HashMap<>();

        String emitterId1 = "1" + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        String emitterId2 = "2" + "_" + "1000000000000";
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);
        newEmitters.put(emitterId1, emitter1);
        newEmitters.put(emitterId2, emitter2);
        emitterRepository.saveAll(newEmitters);

        Map<String, SseEmitter> emitters = emitterRepository.findAll();

        // When
        sseHeartbeatScheduler.sendHeartbeat();

        // Then
        verify(sseNotificationNetworkService, times(emitters.size()))
                .sendHeartBeat(any(), any());
    }
}