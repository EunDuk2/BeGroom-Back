package com.example.BeGroom.notification.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //TODO: Autoincrement인데 그러면 병렬로 insert시 어떻게 될까요?(IDENTity 말고 딴것도 공부해보시고 장단점을 파악해보세요)
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

    @Builder
    private MemberNotification(Member member, Notification notification, String metaData, boolean isRead){
        this.member = member;
        this.notification = notification;
        this.isRead = isRead;
        this.metaData = metaData;
    }

}
