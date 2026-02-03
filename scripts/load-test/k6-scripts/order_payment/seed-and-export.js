// 실행: k6 run seed-and-export.js
// 결과: orders.csv 생성

import http from 'k6/http';
import { check } from 'k6';
import { writeFile } from 'k6/experimental/fs';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

export const options = {
    vus: 1,
    iterations: 1,
};

export default async function () {
    const runId = "3";       // id 갱신
    const orderCount = 10;   // 만들 개수

    const payload = JSON.stringify({ runId, orderCount });

    const res = http.post(`${BASE_URL}/seed/run/csv`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const ok = check(res, {
        'seed ok (200)': (r) => r.status === 200,
        'csv response not empty': (r) => (r.body && r.body.trim().length > 0),
    });

    if (!ok) {
        console.error(`seed failed. status=${res.status}`);
        console.error(`body=\n${res.body}`);
        return;
    }

    // 서버 응답이 "문자열"이므로 JSON 파싱하지 말고 그대로 저장
    // 윈도우/엑셀 호환 위해 줄바꿈 통일(Optional)
    const csvText = res.body.replace(/\r?\n/g, '\n');

    await writeFile('./orders.csv', csvText);
    console.log(`csv saved: orders.csv (bytes=${csvText.length})`);
}
