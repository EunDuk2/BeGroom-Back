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

    // [STEP 1] Register
    const registerPayload = JSON.stringify({
        email: user.email,
        name: user.name,
        password: user.password,
        phoneNumber: user.phoneNumber,
        role: user.role
    });

    const registerRes = http.post(`${BASE_URL}/members`, registerPayload, {
        headers: { 'Content-Type': 'application/json' }
    });
    check(registerRes, { 'Sign up': (r) => r.status === 201 || r.status === 409 });

    sleep(1);

    // [STEP 2] Login
    const loginRes = http.post(`${BASE_URL}/auth`, JSON.stringify({
        username: user.email,
        password: user.password
    }), { headers: { 'Content-Type': 'application/json' } });

    const token = loginRes.json('result.token');
    const params = { headers: { Authorization: `Bearer ${token}` } };

    // [STEP 3] Subscribe SSE
    const sseRes = http.get(`${BASE_URL}/noti/subscribe`, params);
    check(sseRes, { 'SSE connection established': (r) => r.status === 200 });

    sleep(Math.random() * 3 + 1);
}