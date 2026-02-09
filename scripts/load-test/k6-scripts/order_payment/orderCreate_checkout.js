import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const MAX_FAIL_LOGS = Number(__ENV.MAX_FAIL_LOGS || 1);

// 단계별 응답시간(각각 p95)
const createOrderDuration = new Trend('create_order_duration'); // ms
const checkoutDuration = new Trend('checkout_duration');        // ms

// 단계별 실패율(원하면 threshold 걸기)
const createOrderFailed = new Rate('create_order_failed');
const checkoutFailed = new Rate('checkout_failed');

const authData = new SharedArray('auth', function () {
    const text = open('./orders.csv');
    const lines = text.split('\n').map((l) => l.trim()).filter(Boolean);

    // memberId
    const memberIdIdx = lines.indexOf('memberId');
    if (memberIdIdx < 0 || memberIdIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: memberId not found');
    }
    const memberId = Number(lines[memberIdIdx + 1]);
    if (!Number.isFinite(memberId)) throw new Error('memberId invalid');

    // token
    const tokenIdx = lines.indexOf('token');
    if (tokenIdx < 0 || tokenIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: token not found');
    }
    const token = lines[tokenIdx + 1];
    if (!token || token.length < 10) throw new Error('token invalid');

    // productDetailIds
    const pdIdx = lines.indexOf('productDetailIds');
    if (pdIdx < 0 || pdIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: productDetailIds not found');
    }

    const productDetailIds = [];
    for (let i = pdIdx + 1; i < lines.length; i++) {
        const v = lines[i];
        if (v === 'memberId' || v === 'token' || v === 'orderId' || v === 'productDetailIds') break;
        const n = Number(v);
        if (Number.isFinite(n)) productDetailIds.push(n);
    }
    if (productDetailIds.length === 0) {
        throw new Error('orders.csv parse error: productDetailIds empty');
    }

    return [{ memberId, token, productDetailIds }];
});

let failLogCount = 0;

function safeBody(res) {
    const b = res && res.body ? String(res.body) : '';
    return b.length > 2000 ? b.slice(0, 2000) + ' ...[truncated]' : b;
}

function logFail(step, res) {
    if (failLogCount >= MAX_FAIL_LOGS) return;
    failLogCount += 1;
    console.error(`FAIL[${step}] status=${res.status} body=${safeBody(res)}`);
}

function buildCreateOrderPayload(memberId, productDetailIds) {
    return JSON.stringify({
        memberId,
        orderProductList: [
            { productDetailId: productDetailIds[0], orderQuantity: 1 },
            { productDetailId: productDetailIds[1], orderQuantity: 2 },
        ],
    });
}

export const options = {
    scenarios: {
        burst_e2e_once: {
            executor: 'per-vu-iterations',
            vus: Number(__ENV.VUS || 500),
            iterations: 1, // VU당 1회 = 주문 수 == VU 수
            maxDuration: __ENV.MAX_DURATION || '1m',
            // executor: 'constant-vus',
            // duration: '10s',
        },
    },

    thresholds: {
        // 전체(주문+sleep+결제) 플로우 p95: iteration_duration 기준
        iteration_duration: ['p(95)<5500'],

        // 주문 생성 p95
        create_order_duration: ['p(95)<2000'],

        // 결제 p95
        checkout_duration: ['p(95)<3000'],

        // (선택) 실패율도 같이 보면 좋음
        http_req_failed: ['rate<0.01'],
        create_order_failed: ['rate<0.01'],
        checkout_failed: ['rate<0.01'],
    },
};

export default function () {
    const { memberId, token, productDetailIds } = authData[0];

    const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
    };

    // 1) 주문 생성
    const createRes = http.post(
        `${BASE_URL}/orders`,
        buildCreateOrderPayload(memberId, productDetailIds),
        { headers, tags: { step: 'create_order' } }
    );

    // 주문 생성 응답시간만 기록
    createOrderDuration.add(createRes.timings.duration);

    const createOk = check(createRes, {
        'create 201': (r) => r.status === 201,
    });
    createOrderFailed.add(!createOk);

    if (!createOk) {
        logFail('create_order', createRes);
        return;
    }

    const orderId = createRes.json('result.orderId') ?? createRes.json('orderId');
    if (!orderId) {
        logFail('create_order_no_orderId', createRes);
        return;
    }

    // 2) 사용자 플로우 텀
    sleep(0.5);

    // 3) 결제(체크아웃)
    const checkoutRes = http.post(
        `${BASE_URL}/orders/${orderId}/checkout`,
        JSON.stringify({ memberId, paymentMethod: 'POINT' }),
        { headers, tags: { step: 'checkout' } }
    );

    // 결제 응답시간만 기록
    checkoutDuration.add(checkoutRes.timings.duration);

    const checkoutOk = check(checkoutRes, {
        'checkout 200': (r) => r.status === 200,
    });
    checkoutFailed.add(!checkoutOk);

    if (!checkoutOk) {
        logFail('checkout', checkoutRes);
    }
}
