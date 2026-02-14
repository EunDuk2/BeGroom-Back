import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Trend, Rate } from 'k6/metrics';

// =====================
// Env
// =====================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const ORDERS_CSV = __ENV.ORDERS_CSV || './orders.csv';

const VUS = Number(__ENV.VUS || 1500);
const MAX_DURATION = __ENV.MAX_DURATION || '1m';
const MAX_FAIL_LOGS = Number(__ENV.MAX_FAIL_LOGS || 3);

// ✅ 고정 시드 (바꾸면 다른 “고정된 시나리오”가 됨)
const SEED = Number(__ENV.SEED || 123456789);

// Hot/Warm/Cold 주문 비율
const HOT_PCT = Number(__ENV.HOT_PCT || 0.20);
const WARM_PCT = Number(__ENV.WARM_PCT || 0.30);
const COLD_PCT = Number(__ENV.COLD_PCT || 0.50);

// Hot/Warm/Cold 풀 크기
const HOT_POOL_PCT = Number(__ENV.HOT_POOL_PCT || 0.01);
const WARM_POOL_PCT = Number(__ENV.WARM_POOL_PCT || 0.09);

// 사용자 플로우 텀
const THINK_TIME_SEC = Number(__ENV.THINK_TIME_SEC || 0.5);

// 주문 수량
const QTY_A = Number(__ENV.QTY_A || 1);
const QTY_B = Number(__ENV.QTY_B || 2);

// =====================
// Metrics
// =====================
// 기존(클라이언트 관점 전체 duration)
const createOrderDuration = new Trend('create_order_duration'); // ms
const checkoutDuration = new Trend('checkout_duration');        // ms

// ✅ 추가: 서버 처리시간에 더 근접한 waiting (TTFB 유사)
const createOrderWaiting = new Trend('create_order_waiting');   // ms
const checkoutWaiting = new Trend('checkout_waiting');          // ms

const createOrderFailed = new Rate('create_order_failed');
const checkoutFailed = new Rate('checkout_failed');

// =====================
// CSV Parse (single file)
// =====================
function parseOrdersCsv(text) {
    const lines = text
        .split('\n')
        .map((l) => l.trim())
        .filter((l) => l.length > 0);

    const usersHeaderIdx = lines.findIndex(
        (l) => l.replace(/\s+/g, '') === 'memberId,token'
    );
    if (usersHeaderIdx < 0) {
        throw new Error('orders.csv parse error: "memberId,token" header not found');
    }

    const pdHeaderIdx = lines.findIndex(
        (l) => l.replace(/\s+/g, '') === 'productDetailId'
    );
    if (pdHeaderIdx < 0) {
        throw new Error('orders.csv parse error: "productDetailId" header not found');
    }

    if (pdHeaderIdx <= usersHeaderIdx) {
        throw new Error(
            'orders.csv parse error: productDetailId section must come after memberId,token section'
        );
    }

    // users: from usersHeaderIdx+1 until pdHeaderIdx-1
    const users = [];
    for (let i = usersHeaderIdx + 1; i < pdHeaderIdx; i++) {
        const row = lines[i];
        const parts = row.split(',');
        if (parts.length < 2) continue;

        const memberId = Number(parts[0]);
        const token = parts.slice(1).join(',').trim();

        if (!Number.isFinite(memberId)) continue;
        if (!token || token.length < 10) continue;

        users.push({ memberId, token });
    }
    if (users.length === 0) throw new Error('orders.csv parse error: no valid user rows');

    // productDetailIds: from pdHeaderIdx+1 to end
    const productDetailIds = [];
    for (let i = pdHeaderIdx + 1; i < lines.length; i++) {
        const n = Number(lines[i]);
        if (Number.isFinite(n)) productDetailIds.push(n);
    }
    if (productDetailIds.length < 2) {
        throw new Error('orders.csv parse error: need at least 2 productDetailIds');
    }

    return { users, productDetailIds };
}

const parsed = new SharedArray('orders_csv_parsed', function () {
    const text = open(ORDERS_CSV);
    const { users, productDetailIds } = parseOrdersCsv(text);
    return [{ users, productDetailIds }]; // SharedArray는 배열만 반환 가능
});

const USERS = parsed[0].users;
const PRODUCT_DETAIL_IDS = parsed[0].productDetailIds;

// =====================
// Seeded PRNG (xorshift32)
// =====================
function makeRng(seed) {
    let x = seed | 0;
    if (x === 0) x = 123456789; // 0 회피
    return function rand() {
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return (x >>> 0) / 4294967296; // [0,1)
    };
}

// =====================
// Product Pool Split
// =====================
function buildPools(allIds) {
    const total = allIds.length;

    const hotCount = Math.max(1, Math.floor(total * HOT_POOL_PCT));
    const warmCount = Math.max(1, Math.floor(total * WARM_POOL_PCT));
    const hotEnd = Math.min(total, hotCount);
    const warmEnd = Math.min(total, hotEnd + warmCount);

    const hot = allIds.slice(0, hotEnd);
    const warm = allIds.slice(hotEnd, warmEnd);
    const cold = allIds.slice(warmEnd);

    // cold가 비면 warm에서 떼어줌
    if (cold.length === 0) {
        const moved = warm.splice(Math.floor(warm.length / 2));
        cold.push(...moved);
    }

    return { hot, warm, cold };
}

const POOLS = buildPools(PRODUCT_DETAIL_IDS);

// =====================
// Weighted pick
// =====================
function normalizeWeights(a, b, c) {
    const sum = a + b + c;
    if (sum <= 0) return { a: 1 / 3, b: 1 / 3, c: 1 / 3 };
    return { a: a / sum, b: b / sum, c: c / sum };
}

const W = normalizeWeights(HOT_PCT, WARM_PCT, COLD_PCT);

// =====================
// Logging helpers
// =====================
let failLogCount = 0;

function safeBody(res) {
    const b = res && res.body ? String(res.body) : '';
    return b.length > 2000 ? b.slice(0, 2000) + ' ...[truncated]' : b;
}

function logFail(step, res, extra) {
    if (failLogCount >= MAX_FAIL_LOGS) return;
    failLogCount += 1;
    const meta = extra ? ` ${extra}` : '';
    console.error(`FAIL[${step}] status=${res.status}${meta} body=${safeBody(res)}`);
}

// =====================
// Payload
// =====================
function buildCreateOrderPayload(memberId, pdA, pdB) {
    return JSON.stringify({
        memberId,
        orderProductList: [
            { productDetailId: pdA, orderQuantity: QTY_A },
            { productDetailId: pdB, orderQuantity: QTY_B },
        ],
    });
}

// =====================
// k6 options
// =====================
export const options = {
    scenarios: {
        burst_once: {
            executor: 'per-vu-iterations',
            vus: VUS,
            iterations: 1,
            maxDuration: MAX_DURATION,
        },
    },
    thresholds: {
        iteration_duration: ['p(95)<6000'],

        create_order_duration: ['p(95)<2500'],
        checkout_duration: ['p(95)<3000'],

        // ✅ waiting도 같이 보고 싶으면 uncomment
        // create_order_waiting: ['p(95)<2000'],
        // checkout_waiting: ['p(95)<3000'],

        http_req_failed: ['rate<0.01'],
        create_order_failed: ['rate<0.01'],
        checkout_failed: ['rate<0.01'],
    },
};

// =====================
// Test flow
// =====================
export default function () {
    // ✅ VU별 고정 RNG (매 실행마다 동일)
    const rand = makeRng((SEED ^ (__VU * 2654435761)) | 0);

    // VU별 user 고정
    const u = USERS[(__VU - 1) % USERS.length];
    const memberId = u.memberId;
    const token = u.token;

    const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
    };

    function pickPool() {
        const r = rand();
        if (r < W.a) return POOLS.hot;
        if (r < W.a + W.b) return POOLS.warm;
        return POOLS.cold;
    }

    function pickFrom(arr) {
        return arr[Math.floor(rand() * arr.length)];
    }

    function pickTwoDistinctIds() {
        const id1 = pickFrom(pickPool());

        for (let i = 0; i < 10; i++) {
            const id2 = pickFrom(pickPool());
            if (id2 !== id1) return [id1, id2];
        }

        // fallback
        let id2 = id1;
        while (id2 === id1) {
            id2 = PRODUCT_DETAIL_IDS[Math.floor(rand() * PRODUCT_DETAIL_IDS.length)];
        }
        return [id1, id2];
    }

    const [pdA, pdB] = pickTwoDistinctIds();

    // 1) create order
    const createRes = http.post(
        `${BASE_URL}/orders`,
        buildCreateOrderPayload(memberId, pdA, pdB),
        { headers, tags: { step: 'create_order' } }
    );

    // ✅ duration + waiting 둘 다 기록
    createOrderDuration.add(createRes.timings.duration);
    createOrderWaiting.add(createRes.timings.waiting);

    const createOk = check(createRes, { 'create 201': (r) => r.status === 201 });
    createOrderFailed.add(!createOk);

    if (!createOk) {
        logFail('create_order', createRes, `memberId=${memberId} pdA=${pdA} pdB=${pdB}`);
        return;
    }

    const orderId =
        createRes.json('result.orderId') ??
        createRes.json('orderId') ??
        createRes.json('result.id');

    if (!orderId) {
        logFail('create_order_no_orderId', createRes);
        return;
    }

    if (THINK_TIME_SEC > 0) sleep(THINK_TIME_SEC);

    // 2) checkout
    const checkoutRes = http.post(
        `${BASE_URL}/orders/${orderId}/checkout`,
        JSON.stringify({ memberId, paymentMethod: 'POINT' }),
        { headers, tags: { step: 'checkout' } }
    );

    // ✅ duration + waiting 둘 다 기록
    checkoutDuration.add(checkoutRes.timings.duration);
    checkoutWaiting.add(checkoutRes.timings.waiting);

    const checkoutOk = check(checkoutRes, { 'checkout 200': (r) => r.status === 200 });
    checkoutFailed.add(!checkoutOk);

    if (!checkoutOk) {
        logFail('checkout', checkoutRes, `orderId=${orderId} memberId=${memberId}`);
    }
}