package com.example.BeGroom.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
public class BaseEntity {
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedTime;
}
