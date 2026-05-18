import http from 'k6/http';
import {check, sleep} from 'k6';
import {BASE_URL, createMember, createPost, enterChatRoom, jsonHeaders} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';

export const options = {
    scenarios: {
        chat_http: {
            executor: 'constant-vus',
            vus: TARGET_VUS,
            duration: TEST_DURATION,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.02'],
        http_req_duration: ['p(95)<1000'],
        checks: ['rate>0.99'],
    },
};

export default function () {
    const seller = createMember('k6-seller');
    const buyer = createMember('k6-buyer');
    const post = createPost(seller.memberId, {
        title: `k6 chat product ${__VU}-${__ITER}`,
    });

    const room = enterChatRoom(buyer.memberId, seller.memberId, post.postId);

    const rooms = http.get(`${BASE_URL}/api/v1/chat/rooms?memberId=${buyer.memberId}`);
    check(rooms, {
        'chat room list status is 200': (r) => r.status === 200,
    });

    const messages = http.get(`${BASE_URL}/api/v1/chat/messages/${room.roomId}?memberId=${buyer.memberId}`);
    check(messages, {
        'chat messages status is 200': (r) => r.status === 200,
    });

    const read = http.post(`${BASE_URL}/api/v1/chat/rooms/read/${room.roomId}`, JSON.stringify({
        memberId: buyer.memberId,
    }), jsonHeaders());

    check(read, {
        'chat room read status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
