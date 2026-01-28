package com.example.BeGroom.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.example.BeGroom.notification.domain.SseEventMessage.FIRST_CONNECT_UNREAD;
import static com.example.BeGroom.notification.domain.SseEventMessage.RETRY_RECEIVE_NOTIFICATION_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SseEventMessageTest {

    @DisplayName("파라미터에 들어온 인자로 SSE 메시지에 매핑된 결과를 얻을 수 있다.")
    @Test
    void format(){
        // given, when
        String sseMessage1 = RETRY_RECEIVE_NOTIFICATION_SUCCESS.format(3L);
        String sseMessage2 = FIRST_CONNECT_UNREAD.format(5L);

        // then
        assertThat(sseMessage1).isEqualTo("3개의 새 알림이 도착했습니다!");
        assertThat(sseMessage2).isEqualTo("읽지 않은 알림이 5개 있습니다!");
    }


}