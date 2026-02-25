package com.example.BeGroom.notification.event.listener;

import com.example.BeGroom.notification.dto.NetworkMessageDto;

import java.util.List;

public interface NotificationPublisher {
    void publish(List<NetworkMessageDto> data);
}
