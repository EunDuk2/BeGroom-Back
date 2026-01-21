package com.example.BeGroom.notification.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    //TODO: 사용자가 많아진다면 메모리가 부족해질거같은데 어떻게 해야할까?
    public SseEmitter save(String id, SseEmitter emitter) {
            emitters.put(id, emitter);
            return emitter;
    }

    public void deleteById(String id) {
        emitters.remove(id);
    }

    public Map<String, SseEmitter> findAllStartWithById(String memberId) {
        Map<String, SseEmitter> result = new ConcurrentHashMap<>();
        emitters.forEach((id, emitter) -> {
            if(id.startsWith(memberId)){
                result.put(id, emitter);
            }
        });
        return result;
    }

    public Map<String, SseEmitter> findAll() {
        return emitters;
    }
}
