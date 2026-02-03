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
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 10000 },
                { duration: '5m', target: 10000 },
                { duration: '30s', target: 0 },
            ],
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = 'http://172.16.24.179:8080/api';

export default function () {
    // 2. 가상 유저(VU)별 고유 토큰 할당 [cite: 2026-01-05]
    const user = csvData[(__VU - 1) % csvData.length];

    const params = {
        headers: {
            'Authorization': `Bearer ${user.token}`,
            'Accept': 'text/event-stream',
        },
        timeout: '120s',
    };

    const sseRes = http.get(`${BASE_URL}/noti/subscribe`, params);

    check(sseRes, {
        'is status 200': (r) => r.status === 200
    });

    sleep(300);
}