package com.example.BeGroom.settlement.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.dto.res.SettlementTargetDto;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import com.example.BeGroom.settlement.service.processor.SettlementProcessor;
import io.micrometer.core.annotation.Timed;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.example.BeGroom.payment.domain.PaymentStatus.REFUNDED;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementStatus.UNSETTLED;


@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementProcessor settlementProcessor;
    private final EntityManager em;

    //    // 결제 승인 데이터 정산 반영
//    @Timed(value = "settlement.process.time", extraTags = {"step", "1_aggregate-payments"})
//    @Transactional
//    @Override
//    public void aggregateApprovedPayments(){
//        List<Payment> payments = paymentRepository.findApprovedPayments();
//
//        //TODO: insert 쿼리 호출을 줄여보자! (서칭해보세요!!)
//        // batch insert 적용 완료
//        for(Payment payment : payments){
//            Settlement settlement = Settlement.create(payment);
//            settlementRepository.save(settlement);
//
//            payment.markSettled();
//        }
//    }

    @Timed(value = "settlement.process.time", extraTags = {"step", "1_aggregate-payments"})
    @Override
    public void aggregateApprovedPayments(){
        Long lastId = 0L;
        boolean hasNext = true;
        int pageSize = 10000;

        List<CompletableFuture<Void>> futures = new ArrayList<>();  // ✅ 추가

        String executionId = UUID.randomUUID().toString().substring(0, 8);

        //TODO: 톰캣 스레드 하나가 돌아가고있음
        while(hasNext){

            // 1. Slice로 1,000건 조회 (메인 스레드 혼자 진행)
            Slice<SettlementTargetDto> paymentSlice = paymentRepository.findPaymentForSettlement(lastId, Pageable.ofSize(pageSize));

            if(paymentSlice.isEmpty()){
                log.warn("========== 정산 종료 (데이터 없음): {} ==========", executionId);
                break;
            }

            List<SettlementTargetDto> content = paymentSlice.getContent();
            lastId = content.get(content.size() - 1).paymentId();

            CompletableFuture<Void> future = settlementProcessor.processChunkAsync(content);  // ✅ 반환값 받기
            futures.add(future);  // ✅ 저장

            hasNext = paymentSlice.hasNext();
        }

        // ✅ 모든 비동기 작업이 완료될 때까지 대기
        log.info("[{}] 총 {}개 작업 제출 완료, 완료 대기 중...", executionId, futures.size());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.warn("========== 정산 완료: {} (모든 스레드 작업 완료) ==========", executionId);
    }


    // 정산 후 환불 반영
    @Timed(value = "settlement.process.time", extraTags = {"step", "2_update-refund-payments"})
    @Transactional
    @Override
    public void syncRefundedPayments() {

        List<Settlement> targets = settlementRepository.findRefundTargets(REFUNDED, PAYMENT);
        
        for (Settlement settlement : targets) {
            settlement.markRefunded(
                    BigDecimal.valueOf(settlement.getPayment().getAmount())
            );
        }
    }

    // // 미정산 지급 실행
    @Timed(value = "settlement.process.time", extraTags = {"step", "4_payout"})
    @Transactional
    @Override
    public void executeSettlementPayout(){
        // todo : 배치 업데이트 고민하기
        List<Settlement> targets = settlementRepository.findByStatus(UNSETTLED);

        for(Settlement settlement : targets){
            settlement.markSettled();
        }
    }

}
