# k6 성능테스트

Daangn 서버의 주요 REST API와 실시간 통신 기능을 검증하기 위한 k6 성능 테스트 입니다.
테스트는 실제 HTTP/WebSocket/SSE 요청을 보내며, 인증 흐름, 게시글 API, 채팅 API, 알림 스트림이 정상 응답과 목표 응답 시간을 만족하는지 확인합니다.

## 요구사항

- Spring Boot 서버가 `http://localhost:8080`에서 실행 중이어야 합니다.
- 서버가 사용하는 MySQL 데이터베이스가 실행 중이어야 합니다.
- 로컬 환경에 `k6`가 설치되어 있어야 합니다.
- SSE 테스트(`notification-sse.js`)는 `k6/x/sse` 확장이 포함된 k6 실행 환경이 필요합니다.

테스트 스크립트는 실행 중 회원, 게시글, 채팅방 데이터를 생성합니다.

반복 실행 시 테스트 데이터가 누적될 수 있으므로 운영 데이터베이스가 아닌 로컬 또는 전용 테스트 데이터베이스에서 실행하는 것을 권장합니다.

## 폴더 구조

```text
k6/
  data/
    users.json
  lib/
    api.js
  scripts/
    auth-flow.js
    rest-read.js
    posts.js
    chat-http.js
    chat-ws.js
    notification-sse.js
```

`lib/api.js`에는 테스트에서 공통으로 사용하는 함수가 들어 있습니다. 회원 생성, 로그인, 게시글 생성, 채팅방 입장처럼 여러 스크립트에서 반복되는 준비 작업을 이 파일에서 처리합니다.

## 테스트 목표

| 파일                            | 테스트 목표                   | 주요 검증 범위                                                  | 기본 부하                |
|-------------------------------|--------------------------|-----------------------------------------------------------|----------------------|
| `scripts/auth-flow.js`        | 인증 흐름 성능 확인              | 회원가입, 로그인, JWT 인증 후 내 정보 조회                               | 최대 100 VUs까지 ramp-up |
| `scripts/rest-read.js`        | 읽기 중심 REST API 성능 확인     | 내 정보 조회, 게시글 목록 조회, 게시글 상세 조회                             | 100 VUs / 1분         |
| `scripts/posts.js`            | 게시글 CRUD 흐름 확인           | 게시글 생성, 상세 조회, 수정, 목록 조회, 삭제                              | 100 VUs / 1분         |
| `scripts/chat-http.js`        | 채팅 REST API 성능 확인        | 채팅방 목록 조회, 메시지 조회, 읽음 처리                                  | 100 VUs / 1분         |
| `scripts/chat-ws.js`          | 채팅 WebSocket/STOMP 흐름 확인 | WebSocket handshake, STOMP connect/subscribe/send, 메시지 수신 | 100 VUs / 1분         |
| `scripts/notification-sse.js` | 알림 SSE 연결 확인             | SSE 연결, 초기 `notification` 이벤트 수신, 알림 목록 조회                | 100 VUs / 1분         |

## 실행 방법

각 스크립트는 프로젝트 루트에서 다음과 같이 실행합니다.

```bash
k6 run k6/scripts/auth-flow.js
k6 run k6/scripts/rest-read.js
k6 run k6/scripts/posts.js
k6 run k6/scripts/chat-http.js
k6 run k6/scripts/chat-ws.js
```

SSE 테스트는 `k6/x/sse` 확장이 가능한 실행 환경에서 실행합니다.

```bash
k6 run k6/scripts/notification-sse.js
```

## Configuration

기본 서버 주소와 테스트 계정 기본값은 `k6/lib/api.js`에서 관리합니다.

```js
export const BASE_URL = 'http://localhost:8080';
export const DEFAULT_PASSWORD = 'password1234';
```

부하 규모와 실행 시간은 각 스크립트 상단의 상수로 조정합니다.

```js
const TARGET_VUS = 100;
const TEST_DURATION = '1m';
```

현재 스크립트는 환경 변수 주입 없이 파일 안의 상수를 기준으로 실행되도록 구성되어 있습니다.

## Thresholds

테스트는 k6 threshold를 통해 기본 성공 기준을 검증합니다.

- `checks`: 주요 응답 검증 성공률이 99%를 초과해야 합니다.
- `http_req_failed`: HTTP 실패율은 스크립트에 따라 1% 또는 2% 미만이어야 합니다.
- `http_req_duration`: REST 요청의 p95 응답 시간은 700ms 또는 1000ms 미만이어야 합니다.
- `ws_connecting`: WebSocket 연결 p95 시간은 1000ms 미만이어야 합니다.
- `ws_session_duration`: WebSocket 세션 p95 시간은 15000ms 미만이어야 합니다.
- `sse_event`: SSE 테스트에서 이벤트가 1건 이상 수신되어야 합니다.

`rest-read.js`는 조회 API만 별도 태그(`member_info`, `post_list`, `post_detail`)로 나누어 p95 700ms 기준을 적용합니다.

`chat-http.js`는 채팅 REST API를 별도 태그(`chat_room_list`, `chat_message_list`, `chat_room_read`)로 나누어 p95 1000ms 기준을 적용합니다.

나머지 REST 흐름 테스트는 생성, 수정, 삭제 같은 쓰기 작업을 포함하므로 p95 1000ms 기준을 사용합니다.

## Notes

- `auth-flow.js`, `posts.js`, `chat-ws.js`, `notification-sse.js`는 각 반복에서 필요한 테스트 데이터를 직접 생성합니다.
- `rest-read.js`는 `setup()` 단계에서 게시글 데이터를 미리 생성한 뒤 읽기 API를 반복 호출합니다.
- `chat-http.js`는 `setup()` 단계에서 `TARGET_VUS` 수만큼 채팅방 풀을 미리 생성한 뒤 채팅 REST API를 반복 호출합니다.
- `chat-ws.js`는 STOMP 프레임을 문자열로 직접 구성합니다. k6 기본 WebSocket API가 STOMP 전용 클라이언트를 제공하지 않기 때문에, 서버가 기대하는 STOMP wire format에
  맞춰 `CONNECT`, `SUBSCRIBE`, `SEND`, `DISCONNECT` 프레임을 전송합니다.
- `notification-sse.js`는 현재 SSE 연결과 초기 `notification` 이벤트 수신을 검증합니다. 실제 알림 발행까지 포함한 end-to-end 검증은 알림을 발생시킬 수 있는 별도 API나
  테스트 트리거가 필요합니다.