import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./user-data.csv'), { header: true }).data;
});

export const options = {
    tags: {
        application: 'notification-sse-load-test',
    },
    scenarios: {
        sse_subscribe_test: {
            executor: 'constant-vus',
            vus: 100,
            duration: '1m',
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const BASE_URL = 'http://172.16.24.179:8080/api';

export default function () {
    const userIndex = __VU - 1;
    const user = csvData[userIndex];

    const loginPayload = JSON.stringify({
        email: user.email,
        password: user.password,
        role: "USER"
    });

    const loginRes = http.post(`${BASE_URL}/auth`, loginPayload, {
        headers: { 'Content-Type': 'application/json' }
    });

    check(loginRes, { 'Login success': (r) => r.status === 200 });

    const token = loginRes.json('result.token');
    const commonHeaders = {
        Authorization: `Bearer ${token}`,
        'Accept': 'text/event-stream',
    };

    const subscribeRes = http.get(`${BASE_URL}/noti/subscribe`, {
        headers: commonHeaders,
        timeout: '60s'
    });

    check(subscribeRes, {
        'SSE Connection Maintained (200 OK)': (r) => r.status === 200
    });

    sleep(1);
}