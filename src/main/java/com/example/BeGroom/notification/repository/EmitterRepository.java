package com.example.BeGroom.notification.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class EmitterRepository {
    private final Map<Long, Map<String, SseEmitter>> emitterStorage = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, String emitterId, SseEmitter emitter) {
        emitterStorage.computeIfAbsent(memberId, key -> new ConcurrentHashMap<>())
                .put(emitterId, emitter);
        return emitter;
    }

    public void saveAll(Map<String, SseEmitter> emitters) {
        emitters.forEach((emitterId, emitter) -> {
            try {
                String[] parts = emitterId.split("_");
                Long memberId = Long.parseLong(parts[0]);

                save(memberId, emitterId, emitter);
            } catch (Exception e) {
                log.warn("Invalid Emitter ID format: {}", emitterId);
            }
        });
    }

    public void deleteById(String emitterId) {
        if (emitterId == null || !emitterId.contains("_")) {
            return;
        }

        try {
            String[] parts = emitterId.split("_");
            Long memberId = Long.parseLong(parts[0]);

            Map<String, SseEmitter> userEmitters = emitterStorage.get(memberId);

            if (userEmitters != null) {
                userEmitters.remove(emitterId);

                if (userEmitters.isEmpty()) {
                    emitterStorage.remove(memberId);
                }
            }
        } catch (NumberFormatException e) { log.warn("Invalid Emitter ID format: {}", emitterId); }
    }

    public void deleteAll() { emitterStorage.clear(); }

    public Map<String, SseEmitter> findAllStartWithById(Long memberId) {
        Map<String, SseEmitter> userEmitters = emitterStorage.get(memberId);

        return userEmitters != null ? userEmitters : Collections.emptyMap();
    }

    public void deleteAllByMemberId(Long memberId) { emitterStorage.remove(memberId); }

    public Map<String, SseEmitter> findAll() {
        Map<String, SseEmitter> allEmitters = new ConcurrentHashMap<>();
        emitterStorage.values().forEach(allEmitters::putAll);
        return Collections.unmodifiableMap(allEmitters);
    }
}
