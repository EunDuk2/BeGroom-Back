import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

const csv = new SharedArray('orders', function () {
    const text = open('./orders.csv');
    const lines = text.split('\n').map((l) => l.trim()).filter(Boolean);

    // memberId
    const memberIdIdx = lines.indexOf('memberId');
    if (memberIdIdx === -1 || memberIdIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: "memberId" section not found');
    }
    const memberId = Number(lines[memberIdIdx + 1]);

    // token
    const tokenIdx = lines.indexOf('token');
    if (tokenIdx === -1 || tokenIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: "token" section not found');
    }
    const token = lines[tokenIdx + 1];

    // orderIds
    const orderIdIdx = lines.indexOf('orderId');
    if (orderIdIdx === -1 || orderIdIdx + 1 >= lines.length) {
        throw new Error('orders.csv parse error: "orderId" section not found');
    }
    const orderIds = lines.slice(orderIdIdx + 1).map(Number).filter(Number.isFinite);

    if (!Number.isFinite(memberId)) throw new Error('memberId is not a number');
    if (!token || token.length < 10) throw new Error('token looks invalid');
    if (orderIds.length === 0) throw new Error('orderIds empty');

    return [{ memberId, token, orderIds }];
});

export const options = {
    scenarios: {
        checkout_n_orders: {
            executor: 'shared-iterations',
            vus: Number(__ENV.VUS || 100),
            iterations: Number(__ENV.ITERATIONS || csv[0].orderIds.length),
            maxDuration: __ENV.MAX_DURATION || '30m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<300'],
    },
};

export default function () {
    const data = csv[0];

    // shared-iterations에서 "전체 테스트 기준"으로 유일한 번호
    const i = exec.scenario.iterationInTest;

    // iterations를 orderIds.length로 맞추면, 각 주문은 정확히 1번씩만 사용됨
    const orderId = data.orderIds[i];

    const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${data.token}`,
    };

    const res = http.post(
        `${BASE_URL}/orders/${orderId}/checkout`,
        JSON.stringify({ memberId: data.memberId, paymentMethod: 'POINT' }),
        { headers }
    );

    check(res, {
        'checkout 200': (r) => r.status === 200,
    });
}