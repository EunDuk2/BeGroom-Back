package com.example.BeGroom.notification.event;

import com.example.BeGroom.notification.dto.NetworkMessageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class NotificationSavedEvent {
    private final List<NetworkMessageDto> messages;
}
