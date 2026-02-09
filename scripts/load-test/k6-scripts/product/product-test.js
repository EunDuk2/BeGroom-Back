import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100,
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds:{
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    }
}

const BASE_URL = 'http://host.docker.internal:8080/api';

export default function (){
    // [STEP 1] Login
    const loginRes = http.post(`${BASE_URL}/auth`, JSON.stringify({
        username: 'user@begroom.com',
        password: '1234'
    }), { headers: { 'Content-Type': 'application/json' } });
    check(loginRes, {
        'is status 200': (r) => r.status === 200,
        'has token': (r) => r.json('result.token') !== undefined,
    });
    const token = loginRes.json('result.token');
    const params = { headers: { Authorization: `Bearer ${token}` } };
    sleep(1);

    // [STEP 2] SSE subscribe
    const sseRes = http.get(`${BASE_URL}/api/notifications/subscribe`, params);
    check(sseRes, { 'sse connected': (r) => r.status === 200 });

    // [STEP 3] Put an item in cart
    const cartRes = http.post(`${BASE_URL}/cart`, JSON.stringify({ productId: 1 }), params);
    check(cartRes, { 'added to cart': (r) => r.status === 201 });
    sleep(1);

    // [STEP 4] Order products
    const orderRes = http.post(`${BASE_URL}/api/orders`, JSON.stringify({ orderId: 1 }), params);
    check(orderRes, { 'order completed': (r) => r.status === 200 });
    sleep(1);
}