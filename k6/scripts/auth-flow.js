import http from 'k6/http';
import {check, sleep} from 'k6';
import {authHeaders, BASE_URL, createMember} from '../lib/api.js';

const TARGET_VUS = 100;
const RAMP_UP = '30s';
const STEADY = '1m';
const RAMP_DOWN = '30s';

export const options = {
    scenarios: {
        auth_flow: {
            executor: 'ramping-vus',
            stages: [
                {duration: RAMP_UP, target: TARGET_VUS},
                {duration: STEADY, target: TARGET_VUS},
                {duration: RAMP_DOWN, target: 0},
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
        checks: ['rate>0.99'],
    },
};

export default function () {
    const user = createMember('k6-auth');

    const me = http.get(`${BASE_URL}/api/v1/members`, authHeaders(user.token));
    check(me, {
        'authenticated member info status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
