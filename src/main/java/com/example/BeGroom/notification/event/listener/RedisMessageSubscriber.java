package com.example.BeGroom.notification.event.listener;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("notification")
public class RedisMessageSubscriber implements MessageListener {

    private final NotificationNetworkService notificationNetworkService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonMessage = new String(message.getBody());

            List<NetworkMessageDto> messageList = objectMapper.readValue(
                    jsonMessage,
                    new TypeReference<List<NetworkMessageDto>>() {}
            );

            notificationNetworkService.send(messageList);

        } catch (Exception e) {
            log.error("메시지 처리 중 에러 발생: ", e);
        }
    }
}
