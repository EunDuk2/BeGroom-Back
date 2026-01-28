import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./users-data.csv'), { header: true }).data;
});

export const options = {
    scenarios: {
        notification_stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 100 },
                { duration: '1m', target: 100 },
                { duration: '30s', target: 0 },
            ],
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<300'],
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080/api';

export default function () {
    const user = csvData[__VU % csvData.length];

    // [STEP 1] Login
    const loginRes = http.post(`${BASE_URL}/auth`, JSON.stringify({
        username: user.username,
        password: user.password
    }), { headers: { 'Content-Type': 'application/json' } });

    const token = loginRes.json('result.token');
    const params = { headers: { Authorization: `Bearer ${token}` } };

    // [STEP 2] Subscribe SSE
    const sseRes = http.get(`${BASE_URL}/noti/subscribe`, params);
    check(sseRes, { 'SSE connection established': (r) => r.status === 200 });

    // [STEP 3] Get my notifications
    const historyRes = http.get(`${BASE_URL}/noti`, params);
    check(historyRes, { 'History fetched': (r) => r.status === 200 });

    // [STEP 4] Post notification
    const triggerRes = http.post(`${BASE_URL}/noti/send/inspect`, {
        "startTime": "2026.01.01 00:00",
        "endTime": "2026.01.01 05:00"
    }, params);
    check(triggerRes, { 'Notification triggered': (r) => r.status === 200 });

    sleep(1);

    // [STEP 5] Read notification
    const readRes = http.patch(`${BASE_URL}/noti/all`, null, params);
    check(readRes, { 'Notification read': (r) => r.status === 200 });

    sleep(Math.random() * 3 + 1);
}