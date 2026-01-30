import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./user-data.csv'), { header: true }).data;
});

export const options = {
    tags: {
        application: 'notification-service', // 원하는 이름
    },

    scenarios: {
        admin_trigger: {
            executor: 'constant-vus',
            vus: 1,            // 동시 접속자 5명으로 증가
            duration: '1m',    // 3분 동안 지속 (데이터 쌓기)
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<5000'],
        'http_req_failed': ['rate<0.01'],
    },
};

// const BASE_URL = 'http://172.16.24.179:8080/api';
const BASE_URL = 'http://host.docker.internal:8080/api';
// const BASE_URL = 'http://localhost:8080/api';

export default function () {
    const adminUser = {
        email: "user1@begroom.com",
        password: "1234"
    };

    const loginPayload = JSON.stringify({
        email: adminUser.email,
        password: adminUser.password,
        role: "USER"
    });

    const loginRes = http.post(`${BASE_URL}/auth`, loginPayload, {
        headers: { 'Content-Type': 'application/json' }
    });

    if (loginRes.status !== 201) {
        console.log(`❌ Login Failed!`);
        console.log(`   Status: ${loginRes.status}`);
        console.log(`   Error: ${loginRes.error}`);
        console.log(`   Error Code: ${loginRes.error_code}`);
    }

    check(loginRes, { 'Admin Login success': (r) => r.status === 201 });

    const token = loginRes.json('result.token');
    const commonHeaders = {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
    };

    const triggerPayload = JSON.stringify({
        "startTime": "2026.01.01 00:00",
        "endTime": "2026.01.01 05:00"
    });

    const triggerRes = http.post(`${BASE_URL}/noti/send/inspect`, triggerPayload, {
        headers: commonHeaders,
        timeout: '120s'
    });

    if (triggerRes.status !== 201) {
        console.log(`❌ send Failed!`);
        console.log(`   Status: ${triggerRes.status}`);
        console.log(`   Error: ${triggerRes.error}`);
        console.log(`   Error Code: ${triggerRes.error_code}`);
    }

    check(triggerRes, {
        'Broadcast Triggered (201 OK)': (r) => r.status === 201
    });

    console.log(`Response Time: ${triggerRes.timings.duration} ms`);

    sleep(1);
}