package com.example.BeGroom.product.test;

import com.example.BeGroom.IntegrationTestSupport;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DummyDataGeneratorTest extends IntegrationTestSupport {

    @Autowired
    private DummyDataGenerator dummyDataGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private PerformanceMonitor monitor;

    @BeforeEach
    void setUp() {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        monitor = new PerformanceMonitor(jdbcTemplate, hikariDataSource);
        dummyDataGenerator.setPerformanceMonitor(monitor);
    }

    @DisplayName("사용자는 테스트를 위해 100만개의 상품 더미 데이터를 생성할 수 있다.")
    @Test
    @Commit
    void seedData() {
        // given
        int productCount = 1000000;

        // when
        monitor.start();
        dummyDataGenerator.seedAll(productCount);

        // then
        monitor.printReport();
        System.out.println("✅ " + productCount + "개의 데이터 시딩 완료");

    }

}