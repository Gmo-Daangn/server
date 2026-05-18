import ws from 'k6/ws';
import {check, sleep} from 'k6';
import {BASE_URL, createMember, createPost, enterChatRoom} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';
const SEND_DELAY_MS = 100;
const RECEIVE_TIMEOUT_MS = 5000;

export const options = {
    scenarios: {
        chat_ws: {
            executor: 'constant-vus',
            vus: TARGET_VUS,
            duration: TEST_DURATION,
        },
    },
    thresholds: {
        checks: ['rate>0.99'],
        ws_connecting: ['p(95)<1000'],
        ws_session_duration: ['p(95)<15000'],
    },
};

export default function () {
    const seller = createMember('k6-ws-seller');
    const buyer = createMember('k6-ws-buyer');
    const post = createPost(seller.memberId);
    const room = enterChatRoom(buyer.memberId, seller.memberId, post.postId);

    let stompConnected = false;
    let messageReceived = false;
    let errorFrameReceived = false;
    const subscriptionId = `sub-${__VU}-${__ITER}`;
    const expectedMessage = `k6 websocket message ${__VU}-${__ITER}-${Date.now()}`;

    const url = BASE_URL.replace(/^http/, 'ws') + '/api/ws-stomp';
    const response = ws.connect(url, {}, (socket) => {
        socket.on('open', () => {
            socket.send(stompFrame('CONNECT', {
                'accept-version': '1.2',
                host: 'localhost',
            }));
        });

        socket.on('message', (message) => {
            if (message.startsWith('CONNECTED')) {
                stompConnected = true;
                socket.send(stompFrame('SUBSCRIBE', {
                    id: subscriptionId,
                    destination: `/sub/chat/rooms/${room.roomId}/messages`,
                }));

                socket.setTimeout(() => {
                    socket.send(stompFrame('SEND', {
                        destination: `/pub/chat/rooms/${room.roomId}/messages`,
                        'content-type': 'application/json',
                    }, JSON.stringify({
                        memberId: buyer.memberId,
                        message: expectedMessage,
                    })));
                }, SEND_DELAY_MS);
            }

            if (message.startsWith('MESSAGE') && message.includes(expectedMessage)) {
                messageReceived = true;
                socket.send(stompFrame('DISCONNECT', {}));
                socket.close();
            }

            if (message.startsWith('ERROR')) {
                errorFrameReceived = true;
                socket.close();
            }
        });

        socket.setTimeout(() => {
            socket.close();
        }, RECEIVE_TIMEOUT_MS);
    });

    check(response, {
        'websocket handshake status is 101': (r) => r && r.status === 101,
        'stomp connected': () => stompConnected,
        'stomp message received from subscription': () => messageReceived,
        'stomp error frame was not received': () => !errorFrameReceived,
    });

    sleep(1);
}

function stompFrame(command, headers = {}, body = '') {
    // Spring STOMP 엔드포인트는 null byte로 끝나는 프레임을 기대합니다.
    const headerLines = Object.entries(headers)
        .map(([key, value]) => `${key}:${value}`)
        .join('\n');

    return `${command}\n${headerLines}\n\n${body}\x00`;
}
