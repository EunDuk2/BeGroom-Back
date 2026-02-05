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
                { duration: '1m', target: 100 },
                { duration: '1m', target: 1000 },
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
        'is status 200 (Success)': (r) => r.status === 200,
        'is status 1050 (Timeout)': (r) => r.status === 1050,
    });

    sleep(300);
}