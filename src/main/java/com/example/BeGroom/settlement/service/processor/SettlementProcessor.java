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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.*;
import static com.example.BeGroom.settlement.domain.SettlementStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementProcessor {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final EntityManager em;

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


    // pageSize 만큼 처리 (현재 1,000건 단위)
    @Async("settlementExecutor")    // 비동기 병렬 스레드 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> processChunkAsync(List<SettlementTargetDto> dtos){

        System.out.println("========== 메서드 진입 ==========");
        log.info("Current thread: {}", Thread.currentThread().getName());

        long startTime = System.currentTimeMillis();

        for(SettlementTargetDto dto : dtos){
            BigDecimal feeRate = new BigDecimal("10.00");
            BigDecimal fee = BigDecimal.valueOf(dto.paymentAmount())
                    .multiply(feeRate)
                    .divide(new BigDecimal("100"));
            BigDecimal settlementAmount = BigDecimal.valueOf(dto.paymentAmount()).subtract(fee);

            Settlement settlement = Settlement.builder()
                    .seller(em.getReference(Seller.class, dto.sellerId()))      // em.getReference : 껍데기(Proxy) 객체 생성
                    .payment(em.getReference(Payment.class, dto.paymentId()))
                    .paymentAmount(dto.paymentAmount())
                    .fee(fee)
                    .feeRate(feeRate)
                    .settlementAmount(settlementAmount)
                    .status(UNSETTLED)
                    .settlementPaymentStatus(PAYMENT)
                    .refundAmount(BigDecimal.ZERO)
                    .build();

            settlementRepository.save(settlement);

        }

        // payment 상태 대량 변경
        List<Long> ids = dtos.stream().map(SettlementTargetDto::paymentId).toList();
        paymentRepository.updateSettledStatusByIds(ids);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("[{}] 완료 (DB 작업 없음) - 소요 시간: {}ms",
                Thread.currentThread().getName(), totalTime);

        log.info("스레드 동작 완료 - 처리 건수: {}", dtos.size());
        return CompletableFuture.completedFuture(null);  // ✅ 추가
    }

}
