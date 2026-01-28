package com.example.BeGroom.notification.event.listener;

import com.example.BeGroom.notification.event.NotificationSavedEvent;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationListener {
    private final NotificationNetworkService notificationNetworkService;

    @Async()
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationSaved(NotificationSavedEvent event) {
        notificationNetworkService.send(event.getMessages());
    }
}
