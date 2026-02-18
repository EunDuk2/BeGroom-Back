package com.example.BeGroom.settlement.service;

public interface SchedulerService {

    // 결제 승인 데이터 정산
    void aggregateApprovedPayments();

    // 정산 후, 환불 데이터 반영
    void syncRefundedPayments();

    // 지급 실행
    void executeSettlementPayout();

}
