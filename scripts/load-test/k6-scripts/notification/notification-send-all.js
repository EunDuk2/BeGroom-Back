import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./user-tokens.csv'), { header: true }).data;
});

export const options = {
    tags: {
        application: 'notification-service',
    },

    scenarios: {
        admin_notice_once: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '5m',
        },
    },

    thresholds: {
        'http_req_duration': ['p(100)<2000'],
    },
};

// const BASE_URL = 'http://172.16.24.179:8080/api';
const BASE_URL = 'http://host.docker.internal:8080/api';

export default function () {
    const adminUser = csvData[0];

    const commonHeaders = {
        'Authorization': `Bearer ${adminUser.token}`,
        'Content-Type': 'application/json'
    };

    const triggerPayload = JSON.stringify({
        "startTime": "2026.01.01 00:00",
        "endTime": "2026.01.01 05:00"
    });

    const triggerRes = http.post(`${BASE_URL}/noti/send/inspect`, triggerPayload, {
        headers: commonHeaders,
        timeout: '180s'
    });

    if (triggerRes.status === 200 || triggerRes.status === 201) {
        console.log(`✅ Success! Duration: ${triggerRes.timings.duration} ms`);
    } else {
        console.log(`❌ Failed! Status: ${triggerRes.status}`);
        console.log(`   Response: ${triggerRes.body}`);
    }

    check(triggerRes, {
        'Broadcast Triggered (200 OK)': (r) => r.status === 200 || r.status === 201
    });

    sleep(1);
}