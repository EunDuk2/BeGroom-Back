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
// TODO: 테스트 전용. 실서비스 배포 전 반드시 삭제!!
public class TestDataResetRunner implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {

        log.warn("=== 테스트 데이터 초기화 시작 ===");

        // 첫번째 매서드
//        // 1. 정산 테이블 전체 초기화 (AUTO_INCREMENT 포함)
//        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
//        em.createNativeQuery("TRUNCATE TABLE settlement").executeUpdate();
//        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
//        log.info("settlement TRUNCATE 완료");
//
//        // 2. 결제 테이블 정산 상태 원복
//        int updated = em.createNativeQuery(
//                "UPDATE payment SET is_settled = 0 WHERE is_settled = 1"
//        ).executeUpdate();
//        log.info("payment is_settled 원복 완료: {}건", updated);


        // 두번째 매서드
        // 환불 반영된 settlement 원상복구
        int refundReset = em.createNativeQuery(
                "UPDATE settlement SET settlement_payment_status = 'PAYMENT', refund_amount = 0 WHERE settlement_payment_status = 'REFUND'"
        ).executeUpdate();
        log.info("settlement 환불 상태 원복 완료: {}건", refundReset);



        log.warn("=== 테스트 데이터 초기화 완료 ===");

    }
}
