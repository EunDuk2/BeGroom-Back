package com.example.BeGroom.settlement.service.processor;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementPaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import com.example.BeGroom.settlement.dto.res.SettlementTargetDto;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;

import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.*;
import static com.example.BeGroom.settlement.domain.SettlementStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementProcessor {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final EntityManager em;
    private final JdbcTemplate jdbcTemplate;

//    // pageSize 만큼 처리 (현재 1,000건 단위)
//    @Async("settlementExecutor")    // 비동기 병렬 스레드 실행
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void processChunkAsync(List<Payment> payments){
//        for(Payment payment : payments){
//            Settlement settlement = Settlement.create(payment);
//            settlementRepository.save(settlement);
//
//            payment.markSettled();
//        }
////        // 마지막 데이터의 ID 반환해 다음 Slice의 시작점으로 사용
////        return payments.get(payments.size() - 1).getId();
//        log.info("스레드 동작");
//    }


//    // pageSize 만큼 처리 (현재 1,000건 단위)
//    @Async("settlementExecutor")    // 비동기 병렬 스레드 실행
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public CompletableFuture<Void> processChunkAsync(List<SettlementTargetDto> dtos){
//
//        System.out.println("========== 메서드 진입 ==========");
//        log.info("Current thread: {}", Thread.currentThread().getName());
//
//        long startTime = System.currentTimeMillis();
//
//        for(SettlementTargetDto dto : dtos){
//            BigDecimal feeRate = new BigDecimal("10.00");
//            BigDecimal fee = BigDecimal.valueOf(dto.paymentAmount())
//                    .multiply(feeRate)
//                    .divide(new BigDecimal("100"));
//            BigDecimal settlementAmount = BigDecimal.valueOf(dto.paymentAmount()).subtract(fee);
//
//            Settlement settlement = Settlement.builder()
//                    .seller(em.getReference(Seller.class, dto.sellerId()))      // em.getReference : 껍데기(Proxy) 객체 생성
//                    .payment(em.getReference(Payment.class, dto.paymentId()))
//                    .paymentAmount(dto.paymentAmount())
//                    .fee(fee)
//                    .feeRate(feeRate)
//                    .settlementAmount(settlementAmount)
//                    .status(UNSETTLED)
//                    .settlementPaymentStatus(PAYMENT)
//                    .refundAmount(BigDecimal.ZERO)
//                    .build();
//
//            settlementRepository.save(settlement);
//
//        }
//
//        // payment 상태 대량 변경
//        List<Long> ids = dtos.stream().map(SettlementTargetDto::paymentId).toList();
//        paymentRepository.updateSettledStatusByIds(ids);
//
//        long totalTime = System.currentTimeMillis() - startTime;
//        log.info("[{}] 완료 (DB 작업 없음) - 소요 시간: {}ms",
//                Thread.currentThread().getName(), totalTime);
//
//        log.info("스레드 동작 완료 - 처리 건수: {}", dtos.size());
//        return CompletableFuture.completedFuture(null);  // ✅ 추가
//    }


    @Async("settlementExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> processChunkAsync(List<SettlementTargetDto> dtos){
        log.info("가상 스레드 동작 : {}", Thread.currentThread().getName());
//        log.info("Batch Processing Start: {} records on thread {}", dtos.size(), Thread.currentThread().getName());

        // 1. Settlement 대량 INSERT (JdbcTemplate 활용) - bulk로 들어가고 있음
        String sql = "INSERT INTO settlement " +
                "(seller_id, payment_id, payment_amount, fee, fee_rate, settlement_amount, status, settlement_payment_status, refund_amount, is_aggregated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SettlementTargetDto dto = dtos.get(i);

                // 비즈니스 로직 계산 (DTO 데이터를 기반으로 수행)
                BigDecimal feeRate = new BigDecimal("10.00");
                BigDecimal fee = BigDecimal.valueOf(dto.paymentAmount())
                        .multiply(feeRate)
                        .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                BigDecimal settlementAmount = BigDecimal.valueOf(dto.paymentAmount()).subtract(fee);

                ps.setLong(1, dto.sellerId());
                ps.setLong(2, dto.paymentId());
                ps.setLong(3, dto.paymentAmount());
                ps.setBigDecimal(4, fee);
                ps.setBigDecimal(5, feeRate);
                ps.setBigDecimal(6, settlementAmount);
                ps.setString(7, "UNSETTLED");
                ps.setString(8, "PAYMENT");
                ps.setBigDecimal(9, BigDecimal.ZERO);
            }

            @Override
            public int getBatchSize() {
                return dtos.size();
            }
        });

        // 2. Payment 상태 대량 변경 (기존 @Modifying 쿼리 호출)
        List<Long> ids = dtos.stream().map(SettlementTargetDto::paymentId).toList();
        paymentRepository.updateSettledStatusByIds(ids);

        return CompletableFuture.completedFuture(null);
    }



}
