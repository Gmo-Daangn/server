import http from 'k6/http';
import sse from 'k6/x/sse';
import {check, sleep} from 'k6';
import {BASE_URL, createMember} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';

export const options = {
    scenarios: {
        notification_sse: {
            executor: 'constant-vus',
            vus: TARGET_VUS,
            duration: TEST_DURATION,
        },
    },
    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
        sse_event: ['count>0'],
    },
};

export default function () {
    const user = createMember('k6-notification-sse');
    let opened = false;
    let notificationEventReceived = false;
    let expectedConnectMessageReceived = false;
    let errorReceived = false;

    // 기본 k6 http 모듈은 SSE 이벤트 스트림을 다루기 어려워 k6/x/sse 확장을 사용
    const response = sse.open(`${BASE_URL}/api/v1/notification/sse?memberId=${user.memberId}`, {
        method: 'GET',
        headers: {
            Accept: 'text/event-stream',
        },
        tags: {
            endpoint: 'notification_sse',
        },
    }, (client) => {
        client.on('open', () => {
            opened = true;
        });

        client.on('event', (event) => {
            if (event.name === 'notification') {
                notificationEventReceived = true;
            }

            // 초기 연결 성공 이벤트의 receiverId를 검증
            if (event.data && event.data.includes(`receiverId=${user.memberId}`)) {
                expectedConnectMessageReceived = true;
            }

            client.close();
        });

        client.on('error', () => {
            errorReceived = true;
            client.close();
        });

    });

    check(response, {
        'sse status is 200': (r) => r && r.status === 200,
        'sse content type is event-stream': (r) => {
            const contentType = r && r.headers && r.headers['Content-Type'];
            return Array.isArray(contentType)
                ? contentType.some((value) => value.includes('text/event-stream'))
                : String(contentType || '').includes('text/event-stream');
        },
        'sse open event occurred': () => opened,
        'sse notification event received': () => notificationEventReceived,
        'sse connect message has receiver id': () => expectedConnectMessageReceived,
        'sse error was not received': () => !errorReceived,
    });

    const notifications = http.get(`${BASE_URL}/api/v1/notification?memberId=${user.memberId}`, {
        tags: {
            endpoint: 'notification_list',
        },
    });

    check(notifications, {
        'notification list status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
