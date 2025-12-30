package com.example.BeGroom.notification.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = true)
    private String link;

    @Builder
    public Notification(NotificationType type, String title, String message, String link) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.link = link;
    }

    public static Notification createNotification(NotificationType type, String title, String message, String link) {
        return new Notification(type, title, message, link);
    }
}
