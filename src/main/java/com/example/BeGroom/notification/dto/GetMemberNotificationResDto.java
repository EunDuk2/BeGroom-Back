package com.example.BeGroom.notification.dto;

import com.example.BeGroom.notification.domain.MemberNotification;
import lombok.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberNotificationResDto {

    private long unreadCount;
    private List<NotificationInfo> notifications;

    public static GetMemberNotificationResDto of(List<MemberNotification> entities, long unreadCount) {
        List<NotificationInfo> list = entities.stream()
                .map(NotificationInfo::from)
                .collect(Collectors.toList());

        return GetMemberNotificationResDto.builder()
                .unreadCount(unreadCount)
                .notifications(list)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationInfo {
        private Long id;
        private String type;
        private String title;
        private String message;
        private String link;
        private boolean isRead;
        private LocalDateTime createdTime;

        public static NotificationInfo from(MemberNotification entity) {
            String finalMessage = entity.getNotification().getMessage();

            try {
                if (entity.getMetaData() != null && !entity.getMetaData().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> variables = mapper.readValue(entity.getMetaData(), Map.class);

                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        String key = "${" + entry.getKey() + "}";
                        String value = entry.getValue();

                        finalMessage = finalMessage.replace(key, value);
                    }
                }
            } catch (Exception e) {
                finalMessage = entity.getNotification().getMessage();
            }

            return NotificationInfo.builder()
                    .id(entity.getId())
                    .type(entity.getNotification().getType().name())
                    .title(entity.getNotification().getTitle())
                    .message(finalMessage)
                    .link(entity.getNotification().getLink())
                    .isRead(entity.isRead())
                    .createdTime(entity.getCreatedAt())
                    .build();
        }
    }
}