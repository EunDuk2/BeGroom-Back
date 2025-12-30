package com.example.BeGroom.notification.repository;

import com.example.BeGroom.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {}
