package com.example.BeGroom.notification.repository;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NotificationRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private NotificationRepository notificationRepository;

    @DisplayName("알림을 DB에 기록한다.")
    @Test
    void save(){
        // given
        Notification notification = Notification.builder()
                .type(NotificationType.ORDER)
                .title("test title")
                .message("test message")
                .link("test url")
                .build();

        // when
        Notification result = notificationRepository.save(notification);

        // then
        assertThat(result).isEqualTo(notification);
    }
}