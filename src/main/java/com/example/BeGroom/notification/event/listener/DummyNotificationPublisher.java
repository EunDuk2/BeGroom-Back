package com.example.BeGroom.notification.event.listener;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!main")
public class DummyNotificationPublisher implements NotificationPublisher {
    @Override
    public void publish(List<NetworkMessageDto> data) {}
}
