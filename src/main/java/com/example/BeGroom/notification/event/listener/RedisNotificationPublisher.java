package com.example.BeGroom.notification.event.listener;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@Profile("main")
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationPublisher {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(List<NetworkMessageDto> data) {
        if (!data.isEmpty()) {
            String jsonString = objectMapper.writeValueAsString(data);
            stringRedisTemplate.convertAndSend("notification-topic", jsonString);
        }
    }
}
