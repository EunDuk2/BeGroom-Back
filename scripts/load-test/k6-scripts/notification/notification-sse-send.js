import { check, sleep } from 'k6';
import sse from 'k6/x/sse';
import http from 'k6/http';
import { SharedArray } from 'k6/data';
import { Trend, Counter } from 'k6/metrics';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

const csvData = new SharedArray('users', function () {
    return papaparse.parse(open('./user-tokens.csv'), { header: true }).data;
});

// 커스텀 지표 정의
const msgLatency = new Trend('sse_msg_latency'); // 메시지 수신 지연 시간
const msgCount = new Counter('sse_msg_count');   // 수신된 메시지 수

// export const options = {
//   insecureSkipTLSVerify: true,
//   discardResponseBodies: true, // 응답 바디를 메모리에 담지 않음 (SSE 테스트에 필수)
//   noConnectionReuse: false,     // 커넥션 재사용 (포트 고갈 방지)
//   scenarios: {
//     // (Subscribers)
//     audience: {
//       executor: 'ramping-vus',
//       exec: 'audience',
//       startVUs: 0,
//       stages: [
//         { duration: '7m', target: 30000 }, // 2분 동안 1만 명 연결
//         { duration: '3m', target: 30000 }, // 5분 동안 연결 유지
//       ],
//       gracefulStop: '30s',
//     },
//     director: {
//       executor: 'constant-arrival-rate',
//       exec: 'director',
//       rate: 1, 
//       timeUnit: '15s',
//       duration: '2m',
//       preAllocatedVUs: 1,
//       startTime: '7m30s',
//     },
//   },
// };


//// 테스트용
export const options = {
  insecureSkipTLSVerify: true,
  scenarios: {
    // 1. 청중: 빠르게 50명만 연결하고 짧게 유지
    audience: {
      executor: 'ramping-vus',
      exec: 'audience',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 50 }, // 10초 만에 50명 연결 (Warm-up)
        { duration: '1m', target: 50 }, // 40초 동안 연결 유지 (Director 활동 시간 포함)
      ],
      gracefulStop: '5s', // 종료 시 대기 시간도 단축
    },
    
    // 2. 사회자: 청중 연결 직후부터 20초간만 발송
    director: {
      executor: 'constant-arrival-rate',
      exec: 'director',
      rate: 1, // 1초에 1개씩만 발송 (로그 확인하기 좋게 낮춤)
      timeUnit: '5s',
      duration: '10s', // 딱 20초 동안만 메시지 발송
      preAllocatedVUs: 1,
      startTime: '15s', // Audience가 다 연결된 시점(10초) + 5초 여유 뒤 시작
    },
  },
};

// const SSE_URL = 'http://15.165.77.27:8080/api/noti/subscribe';
// const TRIGGER_URL = 'http://15.165.77.27:8080/api/noti/send/inspect';
// const SSE_URL = 'http://host.docker.internal:8080/api/noti/subscribe';
// const TRIGGER_URL = 'http://host.docker.internal:8080/api/noti/send/inspect';
const SSE_URL = 'https://Notification-Api-Gateway-2143529437.ap-northeast-2.elb.amazonaws.com/api/noti/subscribe';
const TRIGGER_URL = 'https://Notification-Api-Gateway-2143529437.ap-northeast-2.elb.amazonaws.com/api/noti/send/inspect';

// 시나리오 1: 청중 (리스너)
export function audience() {
    const user = csvData[(__VU - 1) % csvData.length];

    const params = {
        headers: {
            'Authorization': `Bearer ${user.token}`,
            'Accept': 'text/event-stream',
        },
    };

  sse.open(SSE_URL, params, (client) => {
    // 연결 성공 시
    client.on('open', () => {
      // console.log('Connected!');
    });

    // 메시지 수신 시 (여기가 핵심 측정 구간)
    client.on('event', (event) => {
        if (!event.data || event.data.trim() === '') {
            return;
        }

        const receivedAt = Date.now();

        try{
            const payload = JSON.parse(event.data);
            if (payload.timestamp) {
                const sentAt = payload.timestamp;
                msgLatency.add(receivedAt - sentAt); // 지연 시간
            }
            msgCount.add(1);
        } catch(e){
            console.log(e);
        }
    });

    client.on('error', (e) => {
       console.log('SSE 연결 에러:', e.error());
    });
  });
}

// 시나리오 2: 사회자 (트리거)
export function director() {
    const user = csvData[1];

    const commonHeaders = {
        'Authorization': `Bearer ${user.token}`,
        'Content-Type': 'application/json'
    };

    const triggerPayload = JSON.stringify({
        "startTime": "2026.01.01 00:00",
        "endTime": "2026.01.01 05:00"
    });

    const triggerRes = http.post(TRIGGER_URL, triggerPayload, {
        headers: commonHeaders,
        timeout: '180s'
    });

    check(triggerRes, {
        'Broadcast Triggered (200 OK)': (r) => r.status === 200 || r.status === 201
    });
}