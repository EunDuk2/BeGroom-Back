package com.example.BeGroom.notification.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "notification_id")
    private Notification notification;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(nullable = true)
    private String metaData;

    public MemberNotification(Member member, Notification notification, String metaData) {
        this.member = member;
        this.notification = notification;
        this.isRead = false;
        this.metaData = metaData;
    }

    public void read() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
