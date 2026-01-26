package com.example.BeGroom.notification.dto;

public record NotificationSendResult(
        String emitterId,
        String eventId,
        boolean isSuccess,
        String errorMessage
) {
    public static NotificationSendResult success(String emitterId, String eventId) {
        return new NotificationSendResult(emitterId, eventId, true, null);
    }

    public static NotificationSendResult failure(String emitterId, String eventId, String message) {
        return new NotificationSendResult(emitterId, eventId, false, message);
    }
}