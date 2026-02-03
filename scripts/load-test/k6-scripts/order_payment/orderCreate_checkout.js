import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const VUS = 200;

// 너무 많은 로그 폭주 방지 (기본 20개)
const MAX_FAIL_LOGS = Number(__ENV.MAX_FAIL_LOGS || 1);

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

    // productDetailIds는 다음 섹션 키(예: orderId) 나오기 전까지 숫자 줄들을 전부 읽음
    const productDetailIds = [];
    for (let i = pdIdx + 1; i < lines.length; i++) {
        const v = lines[i];

        // 다음 섹션 헤더를 만나면 stop (memberId/token/orderId/productDetailIds 같은 키)
        if (v === 'memberId' || v === 'token' || v === 'orderId' || v === 'productDetailIds') break;

        const n = Number(v);
        if (Number.isFinite(n)) productDetailIds.push(n);
    }

    if (productDetailIds.length === 0) {
        throw new Error('orders.csv parse error: productDetailIds empty');
    }

    return [{ memberId, token, productDetailIds }];
});

// 실패 로그 카운터 (VU별)
let __failLogCount = 0;

function safeBody(res) {
    const b = res && res.body ? String(res.body) : '';
    return b.length > 2000 ? b.slice(0, 2000) + ' ...[truncated]' : b;
}

function logFail(step, res) {
    if (__failLogCount >= MAX_FAIL_LOGS) return;
    __failLogCount += 1;

    console.error(
        `FAIL[${step}] status=${res.status} body=${safeBody(res)}`
    );
}

function buildCreateOrderPayload(memberId, productDetailIds) {
    // 지금 csv가 45,46 두 개라서 예시 수량을 1,2로 매칭
    // 필요하면 수량도 csv로 빼도 됨
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
            vus: VUS,
            iterations: 1,
            maxDuration: __ENV.MAX_DURATION || '1m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<3000'],
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

    const createOk = check(createRes, {
        'create 200/201': (r) => r.status === 200 || r.status === 201,
    });

    if (!createOk) {
        logFail('create_order', createRes);
        return;
    }

    // 2) orderId 추출 (프로젝트 응답 포맷에 따라 둘 중 하나)
    const orderId = createRes.json('result.orderId') ?? createRes.json('orderId');
    if (!orderId) {
        logFail('create_order_no_orderId', createRes);
        return;
    }

    sleep(0.5);

    // 3) 결제(체크아웃)
    const checkoutRes = http.post(
        `${BASE_URL}/orders/${orderId}/checkout`,
        JSON.stringify({ memberId, paymentMethod: 'POINT' }),
        { headers, tags: { step: 'checkout' } }
    );

    const checkoutOk = check(checkoutRes, {
        'checkout 200': (r) => r.status === 200,
    });

    if (!checkoutOk) {
        logFail('checkout', checkoutRes);
    }
}