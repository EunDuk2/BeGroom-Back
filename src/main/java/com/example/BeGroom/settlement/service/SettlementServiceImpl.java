package com.example.BeGroom.settlement.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.factory.SettlementFactory;
import com.example.BeGroom.settlement.dto.res.*;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import com.example.BeGroom.settlement.repository.daily.DailySettlementRepository;
import com.example.BeGroom.settlement.repository.monthly.MonthlySettlementRepository;
import com.example.BeGroom.settlement.repository.weekly.WeeklySettlementRepository;
import com.example.BeGroom.settlement.repository.yearly.YearlySettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.BeGroom.payment.domain.PaymentStatus.*;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final DailySettlementRepository dailySettlementRepository;
    private final WeeklySettlementRepository weeklySettlementRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;
    private final YearlySettlementRepository yearlySettlementRepository;
    private final PaymentRepository paymentRepository;

    // 정산 요약 정보 조회
    @Override
    public SettlementManageResDto getSettlementManage(Long sellerId){
        // 결제금액
        Long totalPaymentAmount = settlementRepository.getTotalPaymentAmountBySeller(sellerId);
        // 환불금액
        BigDecimal totalRefundAmount = settlementRepository.getTotalRefundtAmountBySeller(sellerId);
        // 수수료
        BigDecimal totalFeeAmount = settlementRepository.getTotalFeeAmountBySeller(sellerId);
        // 정산금액
        BigDecimal totalSettlementAmount = settlementRepository.getTotalSettlementAmountBySeller(sellerId);

        return new SettlementManageResDto(
                totalPaymentAmount,
                totalRefundAmount,
                totalFeeAmount,
                totalSettlementAmount);
    }

    // 건별 정산 집계 조회
    @Override
    public Page<ProductSettlementResDto> getProductSettlement(Long sellerId, LocalDate startDate, LocalDate endDate, int page){
        Pageable pageable = PageRequest.of(page, 15);
        // 건별 정산 리스트
        Page<Settlement> settlements =
                settlementRepository.findProductSettlementListBySeller(sellerId, startDate, endDate, pageable);

        return settlements.map(s -> new ProductSettlementResDto(
                s.getId(),
                s.getCreatedAt().toLocalDate(),
                s.getPaymentAmount(),
                s.getRefundAmount(),
                s.getFee(),
                s.getSettlementAmount(),
                s.getStatus()
        ));
    }


    // 일별 정산 집계 조회
    @Override
    public Page<DailySettlementResDto> getDailySettlement(Long sellerId, int page){
        Pageable pageable = PageRequest.of(page, 15);
        return dailySettlementRepository.findDailySettlement(sellerId, pageable);
    }


    // 주차별 정산 집계 조회
    @Override
    public Page<WeeklySettlementResDto> getWeeklySettlement(Long sellerId, int page){
        Pageable pageable = PageRequest.of(page, 15);
        return weeklySettlementRepository.findWeeklySettlement(sellerId, pageable);
    }

    // 월별 정산 집계 조회
    @Override
    public Page<MonthlySettlementResDto> getMonthlySettlement(Long sellerId, int page){
        Pageable pageable = PageRequest.of(page, 15);
        return monthlySettlementRepository.findMonthlySettlement(sellerId, pageable);
    }

    // 연도별 정산 집계 조회
    @Override
    public Page<YearlySettlementResDto> getYearlySettlement(Long sellerId, int page){
        Pageable pageable = PageRequest.of(page, 15);
        return yearlySettlementRepository.findYearlySettlement(sellerId, pageable);
    }

    // 결제 승인 데이터 정산 반영
    @Transactional
    @Override
    public void aggregateApprovedPayments(){
        List<Payment> payments = paymentRepository.findApprovedPayments(APPROVED);

        //TODO: insert 쿼리 호출을 줄여보자! (서칭해보세요!!) - bulk insert
        for(Payment payment : payments){
            Settlement settlement = Settlement.create(payment);
//            Settlement settlement = SettlementFactory.create(payment);
            settlementRepository.save(settlement);

            payment.markSettled();
        }
    }

    // 정산 후 환불 반영
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
    @Transactional
    @Override
    public void executeSettlementPayout(){

        List<Settlement> targets = settlementRepository.findUnsettledTargets(UNSETTLED);

        for(Settlement settlement : targets){
            settlement.markSettled();
        }
    }

    // csv 내보내기
    @Override
    public void writeDailySettlementCsv(Long sellerId, PrintWriter writer)throws IOException {
        //TODO: 페이징 쿼리 필요, 1억개 가져온다고 가정하면 조회하는것도 느리고.. 그리고 메모리도 용량이 커지겟져(OOM)
        List<DailySettlementCsvDto> settlementCsvDtos =
                settlementRepository.findAllDailySettlementBySeller(sellerId);

        // BOM(엑셀 한글 깨짐 방지)
        writer.write("\uFEFF");

        // Header
        writer.write("정산일자,결제금액,수수료,정산금액,환불금액\n");

        for (DailySettlementCsvDto s : settlementCsvDtos){
            writer.write(
                    s.getSettlementDate() + "," +
                            s.getPaymentAmount() + "," +
                            s.getFee() + "," +
                            s.getSettlementAmount() + "," +
                            s.getRefundAmount() + "\n"
            );
        }
        // 버퍼 -> HTTP Response -> 브라우저
        writer.flush();
    }
}
