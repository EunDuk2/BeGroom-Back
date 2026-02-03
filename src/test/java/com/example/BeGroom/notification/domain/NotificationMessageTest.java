package com.example.BeGroom.notification.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NotificationMessageTest {

    @ParameterizedTest(name = "{0}의 메시지는 ''{1}''이어야 한다")
    @MethodSource("provideMessageData")
    void message_mapping_test(NotificationMessage notificationMessage, String expectedMessage) {
        // Given & When & Then
        assertThat(notificationMessage.getMessage()).isEqualTo(expectedMessage);
    }

    private static Stream<Arguments> provideMessageData() {
        return Stream.of(
                Arguments.of(NotificationMessage.SSE_CONNECTED, "SSE CONNECTED"),
                Arguments.of(NotificationMessage.NEW_NOTIFICATION, "새로운 알림이 도착했습니다!")
        );
    }
}