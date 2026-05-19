import http from 'k6/http';
import {check, sleep} from 'k6';
import {BASE_URL, createMember, createPost, enterChatRoom, jsonHeaders} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';
const CHAT_ROOM_POOL_SIZE = TARGET_VUS;

export const options = {
    scenarios: {
        chat_http: {
            executor: 'constant-vus',
            vus: TARGET_VUS,
            duration: TEST_DURATION,
        },
    },
    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.02'],
        'http_req_duration{endpoint:chat_room_list}': ['p(95)<1000'],
        'http_req_duration{endpoint:chat_message_list}': ['p(95)<1000'],
        'http_req_duration{endpoint:chat_room_read}': ['p(95)<1000'],
    },
};

export function setup() {
    const rooms = [];

    for (let index = 0; index < CHAT_ROOM_POOL_SIZE; index += 1) {
        const seller = createMember('k6-chat-seller');
        const buyer = createMember('k6-chat-buyer');
        const post = createPost(seller.memberId, {
            title: `k6 chat product ${index}`,
        });
        const room = enterChatRoom(buyer.memberId, seller.memberId, post.postId);

        rooms.push({
            buyerId: buyer.memberId,
            roomId: room.roomId,
        });
    }

    return {rooms};
}

export default function (data) {
    const room = data.rooms[(__VU - 1) % data.rooms.length];

    const rooms = http.get(`${BASE_URL}/api/v1/chat/rooms?memberId=${room.buyerId}`, {
        tags: {
            endpoint: 'chat_room_list',
        },
    });
    check(rooms, {
        'chat room list status is 200': (r) => r.status === 200,
    });

    const messages = http.get(`${BASE_URL}/api/v1/chat/messages/${room.roomId}?memberId=${room.buyerId}`, {
        tags: {
            endpoint: 'chat_message_list',
        },
    });
    check(messages, {
        'chat messages status is 200': (r) => r.status === 200,
    });

    const read = http.post(`${BASE_URL}/api/v1/chat/rooms/read/${room.roomId}`, JSON.stringify({
        memberId: room.buyerId,
    }), {
        ...jsonHeaders(),
        tags: {
            endpoint: 'chat_room_read',
        },
    });

    check(read, {
        'chat room read status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
