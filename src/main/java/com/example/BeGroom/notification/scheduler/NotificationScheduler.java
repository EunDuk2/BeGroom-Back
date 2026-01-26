package com.example.BeGroom.notification.scheduler;

import com.example.BeGroom.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.BeGroom.notification.domain.NotificationTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * 2시간마다 실행
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void sendPeriodicNotifications() {
        try {
            Map<String, String> eventData = new HashMap<>();
            notificationService.sendToAllMembers(NotificationTemplate.AD_FREE_CASH_EVENT.getId(), eventData);
        } catch (Exception e) {
            log.error("스케줄러 실행 중 에러 발생", e);
        }
    }
}