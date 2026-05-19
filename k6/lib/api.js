import http from 'k6/http';
import {check, fail} from 'k6';

// 테스트 대상 서버 주소
export const BASE_URL = 'http://localhost:8080';
export const DEFAULT_PASSWORD = 'password1234';

const JSON_HEADERS = {
    'Content-Type': 'application/json',
};

export function jsonHeaders(extraHeaders = {}) {
    return {
        headers: {
            ...JSON_HEADERS,
            ...extraHeaders,
        },
    };
}

export function uniqueEmail(prefix = 'k6-user') {
    // 회원가입 시 중복 이메일처리 방지를 위해 랜덤으로 새 이메일을 생성
    const vu = typeof __VU === 'undefined' ? 'setup' : __VU;
    const iter = typeof __ITER === 'undefined' ? 0 : __ITER;
    const suffix = Math.random().toString(36).slice(2, 10);

    return `${prefix}-${Date.now()}-${vu}-${iter}-${suffix}@test.com`;
}

export function signupUser(email = uniqueEmail(), password = DEFAULT_PASSWORD) {
    const nickname = email.split('@')[0];
    const response = http.post(`${BASE_URL}/api/v1/auth`, JSON.stringify({
        email,
        nickname,
        password,
        address: {
            city: 'Seoul',
            district: 'Gangnam-gu',
            town: 'Yeoksam-dong',
        },
    }), jsonHeaders());

    requireChecks(response, {
        'signup status is 200': (r) => r.status === 200,
        'signup returns member id': (r) => extractMemberId(r) > 0,
    }, 'signup');

    return {
        email,
        password,
        memberId: extractMemberId(response),
        response,
    };
}

export function loginUser(email, password = DEFAULT_PASSWORD) {
    const response = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email,
        password,
    }), jsonHeaders());

    requireChecks(response, {
        'login status is 200': (r) => r.status === 200,
        'login returns access token': (r) => !!responseValue(r, 'data.accessToken'),
    }, 'login');

    const grantType = responseValue(response, 'data.grantType') || 'Bearer';
    const accessToken = responseValue(response, 'data.accessToken');

    return {
        grantType,
        accessToken,
        authorization: `${grantType} ${accessToken}`,
        response,
    };
}

export function createMember(prefix = 'k6-user') {
    const user = signupUser(uniqueEmail(prefix));
    if (!user.memberId) {
        fail('signup succeeded without a parseable member id');
    }

    return {
        ...user,
        token: loginUser(user.email, user.password),
    };
}

export function authHeaders(token) {
    // 로그인 응답의 grantType/accessToken 형식에 맞춰 인증 헤더를 생성
    return jsonHeaders({
        Authorization: token.authorization,
    });
}

export function createPost(memberId, overrides = {}) {
    const response = http.post(`${BASE_URL}/api/v1/posts`, JSON.stringify({
        title: overrides.title || `k6 test post ${Date.now()}`,
        content: overrides.content || 'Created by k6 smoke/performance scripts.',
        price: overrides.price ?? 10000,
        memberId,
    }), jsonHeaders());

    requireChecks(response, {
        'create post status is 200': (r) => r.status === 200,
        'create post returns post id': (r) => Number(responseValue(r, 'data.postId')) > 0,
    }, 'create post');

    return {
        postId: Number(responseValue(response, 'data.postId')),
        response,
    };
}

export function enterChatRoom(memberId, targetMemberId, productId) {
    const response = http.post(`${BASE_URL}/api/v1/chat/rooms/enter`, JSON.stringify({
        memberId,
        targetMemberId,
        productId,
    }), jsonHeaders());

    requireChecks(response, {
        'chat room enter status is 200': (r) => r.status === 200,
        'chat room id exists': (r) => Number(responseValue(r, 'data.roomId')) > 0,
    }, 'chat room enter');

    return {
        roomId: Number(responseValue(response, 'data.roomId')),
        response,
    };
}

function extractMemberId(response) {
    const message = responseValue(response, 'data');
    if (typeof message !== 'string') {
        return 0;
    }

    const match = message.match(/ID\s*:\s*(\d+)/);
    return match ? Number(match[1]) : 0;
}

export function responseValue(response, path) {
    // 네트워크 실패처럼 body가 비는 경우에도 테스트가 터지지 않고 check 실패로 처리
    try {
        return response.json(path);
    } catch (_error) {
        return undefined;
    }
}

function requireChecks(response, checks, label) {
    const passed = check(response, checks);
    if (!passed) {
        fail(`${label} failed: status=${response.status}`);
    }
}
