import http from 'k6/http';
import {check, sleep} from 'k6';
import {BASE_URL, createMember, createPost, jsonHeaders} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';

export const options = {
    scenarios: {
        post_crud: {
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
    const user = createMember('k6-posts');
    const post = createPost(user.memberId, {
        title: `k6 post ${__VU}-${__ITER}`,
        price: 5000 + __ITER,
    });

    const detail = http.get(`${BASE_URL}/api/v1/posts/${post.postId}`);
    check(detail, {
        'post detail status is 200': (r) => r.status === 200,
    });

    const update = http.put(`${BASE_URL}/api/v1/posts/${post.postId}`, JSON.stringify({
        title: `k6 updated post ${__VU}-${__ITER}`,
        content: 'Updated by k6.',
        price: 7000 + __ITER,
        memberId: user.memberId,
        status: 'FOR_SALE',
    }), jsonHeaders());

    check(update, {
        'post update status is 200': (r) => r.status === 200,
    });

    const list = http.get(`${BASE_URL}/api/v1/posts?page=0&size=10`);
    check(list, {
        'post list status is 200': (r) => r.status === 200,
    });

    const remove = http.del(`${BASE_URL}/api/v1/posts/${post.postId}?memberId=${user.memberId}`);
    check(remove, {
        'post delete status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
