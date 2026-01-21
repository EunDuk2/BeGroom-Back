package com.example.BeGroom.notification.repository;

import com.example.BeGroom.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EmitterRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private EmitterRepository emitterRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    @DisplayName("사용자의 SSE Emitter 정보를 저장한다.")
    @Test
    void saveEmitter() {
        // given
        String memberId = "1";
        String currentTimeMillis = "1768993200123";
        String emitterId = memberId + "_" + currentTimeMillis;
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // when
        emitterRepository.save(emitterId, emitter);

        // then
        Map<String, SseEmitter> emitters = emitterRepository.findAll();
        assertThat(emitters).hasSize(1).containsKey(emitterId);
    }

    @DisplayName("사용자의 SSE Emitter 정보를 삭제한다.")
    @Test
    void removeEmitter() {
        // given
        String memberId = "1";
        String currentTimeMillis = "1768993200123";
        String emitterId = memberId + "_" + currentTimeMillis;
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(emitterId, emitter);

        // when
        emitterRepository.deleteById(emitterId);

        // then
        Map<String, SseEmitter> emitters = emitterRepository.findAll();
        assertThat(emitters).hasSize(0);
    }
}