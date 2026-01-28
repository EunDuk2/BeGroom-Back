package com.example.BeGroom.notification.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class MessageUtilTest {

    @DisplayName("문자열 메시지를 key 값이 message인 HashMap 메시지로 변환한다.")
    @Test
    void createMessageByHashMap() {
        // given
        String message = "test message";

        // when
        Map<String, Object> result =  MessageUtil.createMessageByHashMap(message);

        // then
        assertThat(result)
                .containsEntry("message", "test message")
                .hasSize(1);
    }

    @DisplayName("문자열 메시지를 key 값이 message인 HashMap 메시지로 변환할 때, 문자열 내용은 필수이다.")
    @Test
    void createMessageByHashMapWithoutMessageContent() {
        // given
        String message = "";

        // when, then
        assertThatThrownBy( ()-> MessageUtil.createMessageByHashMap(message))
                .isInstanceOf( IllegalArgumentException.class)
                .hasMessage("메시지가 비어있습니다.");
    }

    @DisplayName("멤버의 Id와 CurrentTimeMillis를 이용해서 EmitterId를 만든다.")
    @Test
    void makeEmitterId(){
        // given
        Long memberId = 1L;
        LocalDateTime currentTime = LocalDateTime.of(2026, Month.JANUARY,1,0,0);

        // when
        String result = MessageUtil.makeEmitterId(memberId, currentTime);

        // then
        assertThat(result).isEqualTo("1_1767193200000");
    }
}