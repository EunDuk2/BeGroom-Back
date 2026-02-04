import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// 커스텀 지표: 정산 처리 시간 기록
const settlementTime = new Trend('settlement_processing_time');

export const options = {
    vus: 1,          // 스케줄러 로직이므로 1명으로 시작
    iterations: 1,   // 딱 한 번 실행해서 100만 건 처리 시간 확인
};

export default function () {
    const start = Date.now();

    // 100만 건 처리를 위해 k6가 10분 동안 참고 기다리게 설정
    const params = {
        timeout: '600s', // 10분
    };

    // 100만 건 적재 API 호출
    const res = http.post(
        'http://host.docker.internal:8080/api/test/settlement/aggregate-payments',
        null,
        params);
    console.log('Response Status:', res.status);
    console.log('Response Body:', res.body);

    const end = Date.now();
    settlementTime.add(end - start);

    check(res, {
        '100만 건 처리 성공': (r) => r.status === 200,
        '에러 없음': (r) => r.status !== 500,
    });
}