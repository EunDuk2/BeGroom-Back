package com.example.BeGroom.notification.repository;

import com.example.BeGroom.notification.domain.MemberNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

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
                        "SELECT m.id, ?, ?, false, NOW() " +
                        "FROM member m " +
                        "WHERE m.id BETWEEN ? AND ?";

        long startTime = System.currentTimeMillis();

        int insertedCount = jdbcTemplate.update(sql, templateId, convertToJson(variables), startId, endId);

        System.out.printf("[Range: %d-%d] %d건 저장 (소요시간: %dms)%n",
                startId, endId, insertedCount, (System.currentTimeMillis() - startTime));
    }

    private String convertToJson(Map<String, String> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception e) {
            throw new RuntimeException("Convert to json failed: ", e);
        }
    }
}