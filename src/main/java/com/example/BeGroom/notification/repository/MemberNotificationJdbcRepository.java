package com.example.BeGroom.notification.repository;

import com.example.BeGroom.common.log.MeasureBlocking;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.BeGroom.notification.domain.SseEventMessage.COMMON_RECEIVE_NOTIFICATION_SUCCESS;
import static java.time.LocalTime.now;

@Repository
@RequiredArgsConstructor
public class MemberNotificationJdbcRepository {
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<MemberNotification> notifications) {
        String sql = "INSERT INTO member_notification " +
                "(member_id, notification_id, is_read, meta_data, created_at, updated_at) " +
                "VALUES (?, ?, false, ?, now(), null)";

        jdbcTemplate.batchUpdate(sql,
                notifications,
                1000,
                (PreparedStatement ps, MemberNotification notification) -> {
                    ps.setLong(1, notification.getMember().getId());
                    ps.setLong(2, notification.getNotification().getId());
                    ps.setString(3, notification.getMetaData());
                });
    }

    public void partitionInsert(Long templateId, Map<String, String> variables, long startId, long endId) {
        String sql =
                "INSERT INTO member_notification (member_id, notification_id, meta_data, is_read, created_at) " +
                        "SELECT m.id, ?, ?, false, ? " +
                        "FROM member m " +
                        "WHERE m.id BETWEEN ? AND ?";

        long startTime = System.currentTimeMillis();

        int insertedCount = jdbcTemplate.update(sql, templateId, convertToJson(variables), LocalDateTime.now(), startId, endId);

        System.out.printf("[Range: %d-%d] %d건 저장 (소요시간: %dms)%n",
                startId, endId, insertedCount, (System.currentTimeMillis() - startTime));
    }

    public List<NetworkMessageDto> findNetworkMessageDtoByRange(Long templateId, long minMemberId, long maxMemberId, LocalDateTime batchStartTime) {
        String sql =
                "SELECT n.member_id, n.id " +
                        "FROM member_notification n " +
                        "WHERE n.notification_id = ? AND n.member_id BETWEEN ? AND ? " +
                        "AND n.created_at >= ?";

        System.out.printf("[Range: TemplateId=%d, MemberId=%d-%d]%n in %s actual time %s", templateId, minMemberId, maxMemberId, batchStartTime, now());

        return jdbcTemplate.query(sql, (rs, rowNum) -> NetworkMessageDto.builder()
                        .receiverId(rs.getLong("member_id"))
                        .eventId(String.valueOf(rs.getLong("id")))
                        .eventName("notification")
                        .data(MessageUtil.createMessageByHashMap(COMMON_RECEIVE_NOTIFICATION_SUCCESS.getMessageTemplate()))
                        .build(),
                templateId, minMemberId, maxMemberId, batchStartTime
        );
    }

    private String convertToJson(Map<String, String> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            throw new RuntimeException("Convert to json failed: ", e);
        }
    }
}