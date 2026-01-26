package com.example.BeGroom.notification.repository;

import com.example.BeGroom.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmitterRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private EmitterRepository emitterRepository;

    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @AfterEach
    void tearDown() {
        emitterRepository.deleteAll();
    }

    @DisplayName("맴버의 SSE Emitter 정보를 저장한다.")
    @Test
    void saveEmitter() {
        // given
        Long memberId = 1L;
        String currentTimeMillis = "1768993200123";
        String emitterId = memberId + "_" + currentTimeMillis;
        SseEmitter emitter = new SseEmitter(defaultTimeout);

        // when
        emitterRepository.save(emitterId, emitter);

        // then
        Map<String, SseEmitter> emitters = emitterRepository.findAll();
        assertThat(emitters).containsOnlyKeys(emitterId);
    }

    @DisplayName("맴버의 SSE Emitter 정보를 저장할 때, 중복된 EmitterId는 존재하지 않는다.")
    @Test
    void saveEmitterWithDuplicatedId() {
        // given
        Long memberId = 1L;
        String currentTimeMillis = "1768993200123";
        String emitterId = memberId + "_" + currentTimeMillis;
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);
        emitterRepository.save(emitterId, emitter1);
        emitterRepository.save(emitterId, emitter2);

        // when, then
        assertThat(emitterRepository.findAll()).containsOnlyKeys(emitterId);
    }

    @DisplayName("멤버의 SSE Emitter 정보를 모두 저장한다.")
    @Test
    void saveAllEmitters() {
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String currentTimeMillis1 = "1000000000000";
        String currentTimeMillis2 = "1000000000001";
        String emitter1Id = member1Id + "_" + currentTimeMillis1;
        String emitter2Id = member2Id + "_" + currentTimeMillis2;
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitter1Id, emitter1);
        emitters.put(emitter2Id, emitter2);

        // when
        emitterRepository.saveAll(emitters);

        // then
        Map<String, SseEmitter> result = emitterRepository.findAll();
        assertThat(result).containsOnlyKeys(emitter1Id, emitter2Id);
    }

    @DisplayName("동시에 100명이 Emitter 저장을 요청해도 데이터가 유실되지 않는다.")
    @Test
    void saveEmitterWith100Request() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            long memberId = i;

            executorService.submit(() -> {
                try {
                    String emitterId = memberId + "_" + System.currentTimeMillis();
                    SseEmitter emitter = new SseEmitter(60L * 1000L); // 1분 타임아웃

                    emitterRepository.save(emitterId, emitter);

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Map<String, SseEmitter> emitters = emitterRepository.findAll();
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(emitters).hasSize(threadCount);
    }

    @DisplayName("멤버의 SSE Emitter 정보를 삭제한다.")
    @Test
    void removeEmitter() {
        // given
        Long memberId = 1L;
        String currentTimeMillis = "1768993200123";
        String emitterId = memberId + "_" + currentTimeMillis;
        SseEmitter emitter = new SseEmitter(defaultTimeout);
        emitterRepository.save(emitterId, emitter);

        // when
        emitterRepository.deleteById(emitterId);

        // then
        Map<String, SseEmitter> emitters = emitterRepository.findAll();
        assertThat(emitters).hasSize(0);
    }

    @DisplayName("모든 SSE Emitter 정보를 삭제한다.")
    @Test
    void removeAllEmitters() {
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String currentTimeMillis1 = "1000000000000";
        String currentTimeMillis2 = "1000000000001";
        String emitter1Id = member1Id + "_" + currentTimeMillis1;
        String emitter2Id = member2Id + "_" + currentTimeMillis2;
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitter1Id, emitter1);
        emitters.put(emitter2Id, emitter2);

        emitterRepository.saveAll(emitters);

        // when
        emitterRepository.deleteAll();

        // then
        Map<String, SseEmitter> result = emitterRepository.findAll();
        assertThat(result).hasSize(0);
    }

    @DisplayName("멤버의 Id로 시작하는 Emitter를 조회한다.")
    @Test
    void findAllStartWithById() {
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String currentTimeMillis1 = "1000000000000";
        String currentTimeMillis2 = "1000000000001";
        String emitter1Id = member1Id + "_" + currentTimeMillis1;
        String emitter2Id = member2Id + "_" + currentTimeMillis2;
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitter1Id, emitter1);
        emitters.put(emitter2Id, emitter2);
        emitterRepository.saveAll(emitters);

        // when
        Map<String, SseEmitter> result = emitterRepository.findAllStartWithById(member1Id);

        // then
        assertThat(result).containsOnlyKeys(emitter1Id);
    }

    @DisplayName("모든 Emitter를 조회한다.")
    @Test
    void findAll() {
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String currentTimeMillis1 = "1000000000000";
        String currentTimeMillis2 = "1000000000001";
        String emitter1Id = member1Id + "_" + currentTimeMillis1;
        String emitter2Id = member2Id + "_" + currentTimeMillis2;
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitter1Id, emitter1);
        emitters.put(emitter2Id, emitter2);
        emitterRepository.saveAll(emitters);

        // when
        Map<String, SseEmitter> result = emitterRepository.findAll();

        // then
        assertThat(result).containsOnlyKeys(emitter1Id, emitter2Id);
    }
}