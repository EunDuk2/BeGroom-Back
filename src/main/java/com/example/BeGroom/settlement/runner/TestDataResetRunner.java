package com.example.BeGroom.settlement.runner;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataResetRunner implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {


//        환경 변수 설정해서 원할 때 실행
//        docker run -e TEST_DATA_RESET=true 이미지명
//        docker run 이미지명

        String resetEnabled = System.getenv("TEST_DATA_RESET");

        if (!"true".equals(resetEnabled)) {
            log.info("TEST_DATA_RESET 비활성화 - 초기화 건너뜀");
            return;
        }

        log.warn("=== 테스트 데이터 초기화 시작 ===");

        // 1. 정산 테이블 전체 초기화 (AUTO_INCREMENT 포함)
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE settlement").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        log.info("settlement TRUNCATE 완료");

        // 2. 결제 테이블 정산 상태 원복
        int updated = em.createNativeQuery(
                "UPDATE payment SET is_settled = 0 WHERE is_settled = 1"
        ).executeUpdate();
        log.info("payment is_settled 원복 완료: {}건", updated);

        log.warn("=== 테스트 데이터 초기화 완료 ===");

    }
}
