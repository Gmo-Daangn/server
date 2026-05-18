import http from 'k6/http';
import {check, sleep} from 'k6';
import {BASE_URL, createMember, createPost} from '../lib/api.js';

const TARGET_VUS = 100;
const TEST_DURATION = '1m';
const SEED_POSTS = 30;

export const options = {
    scenarios: {
        rest_read: {
            executor: 'constant-vus',
            vus: TARGET_VUS,
            duration: TEST_DURATION,
        },
    },
    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.01'],
        'http_req_duration{endpoint:member_info}': ['p(95)<700'],
        'http_req_duration{endpoint:post_list}': ['p(95)<700'],
        'http_req_duration{endpoint:post_detail}': ['p(95)<700'],
    },
};

export function setup() {
    const owner = createMember('k6-rest-read-owner');
    const posts = [];

    for (let index = 0; index < SEED_POSTS; index += 1) {
        const post = createPost(owner.memberId, {
            title: `k6 seed post ${index}`,
            content: 'Seed data for REST read performance tests.',
            price: 1000 + index,
        });
        posts.push(post.postId);
    }

    return {
        authorization: owner.token.authorization,
        posts,
    };
}

export default function (data) {
    const postId = data.posts[Math.floor(Math.random() * data.posts.length)];

    const me = http.get(`${BASE_URL}/api/v1/members`, {
        headers: {
            Authorization: data.authorization,
        },
        tags: {
            endpoint: 'member_info',
        },
    });
    check(me, {
        'member info status is 200': (r) => r.status === 200,
    });

    const list = http.get(`${BASE_URL}/api/v1/posts?page=0&size=20`, {
        tags: {
            endpoint: 'post_list',
        },
    });
    check(list, {
        'post list status is 200': (r) => r.status === 200,
    });

    const detail = http.get(`${BASE_URL}/api/v1/posts/${postId}`, {
        tags: {
            endpoint: 'post_detail',
        },
    });
    check(detail, {
        'post detail status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
