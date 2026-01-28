package com.example.BeGroom.notification.scheduler;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.notification.domain.NotificationTemplate;
import com.example.BeGroom.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationSchedulerTest extends IntegrationTestSupport {

    @MockitoSpyBean
    private NotificationService notificationService;

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Test
    @DisplayName("스케줄러가 실행되면 모든 멤버에게 광고성 알림을 보내야 한다")
    void sendPeriodicNotifications() {
        // Given, When
        notificationScheduler.sendPeriodicNotifications();

        // Then
        verify(notificationService, times(1))
                .sendToAllMembers(eq(NotificationTemplate.AD_FREE_CASH_EVENT.getId()), any(HashMap.class));
    }

    @Test
    @DisplayName("서비스 실행 중 DB 에러가 발생해도 스케줄러는 중단되지 않고 로그를 남겨야 한다")
    void sendPeriodicNotificationsWithException() {
        // Given
        doThrow(new RuntimeException("DB connection failed"))
                .when(notificationService).sendToAllMembers(anyLong(), any());

        // When
        notificationScheduler.sendPeriodicNotifications();

        // Then
        verify(notificationService, times(1)).sendToAllMembers(anyLong(), any());
    }
}