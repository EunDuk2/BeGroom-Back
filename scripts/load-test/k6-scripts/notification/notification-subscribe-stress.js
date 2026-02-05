import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./user-tokens.csv'), { header: true }).data;
});

export const options = {
    scenarios: {
        notification_stress: {
            executor: 'ramping-vus', // 점진적으로 VU를 늘리는 설정
            startVUs: 0,
            stages: [
                { duration: '1m', target: 1000 },  // 1분 동안 1,000명까지 증가
                { duration: '1m', target: 10000 },  // 다음 2분 동안 5,000명까지 증가
                { duration: '1m', target: 20000 },  // 다음 2분 동안 5,000명까지 증가
                { duration: '1m', target: 30000 }, // 최종적으로 10,000명까지 도달
                { duration: '1m', target: 30000 }, // 10,000명 유지하며 버티기 테스트
                { duration: '1m', target: 0 },     // 마지막 1분 동안 서서히 종료
            ],
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080/api';

export default function () {
    const user = csvData[(__VU - 1) % csvData.length];

    const params = {
        headers: {
            'Authorization': `Bearer ${user.token}`,
            'Accept': 'text/event-stream',
        },
        timeout: '60s',
    };

    const sseRes = http.get(`${BASE_URL}/noti/subscribe`, params);

    check(sseRes, {
        'connected or streamed': (r) => r.status === 200 || r.status === 0,
    });

    sleep(300);
}