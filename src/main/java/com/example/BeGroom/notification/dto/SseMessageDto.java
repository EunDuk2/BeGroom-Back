package com.example.BeGroom.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SseMessageDto {
    private Long id;
    private String title;
    private String content;
    private String link;

    @Builder
    public SseMessageDto(Long id, String title, String content, String link) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.link = link;
    }
}
