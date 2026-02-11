package com.example.BeGroom.member.repository;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberBulkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 10000;

    @Transactional
    public void bulkInsert(List<MemberBulkDto> members) {
        String sql = "INSERT INTO member (email, name, password, phone_number, created_at, role) " +
                "VALUES (?, ?, '1234', ?, NOW(), 'USER')";

        jdbcTemplate.batchUpdate(
                sql,
                members,
                BATCH_SIZE,
                (PreparedStatement ps, MemberBulkDto member) -> {
                    ps.setString(1, member.getEmail());
                    ps.setString(2, member.getName());
                    ps.setString(3, member.getPhoneNumber());
                }
        );
    }
}
