# k6 테스트

서버의 주요 API를 실제 실행 환경에서 확인하기 위한 k6 테스트 모음입니다.

## 실행 전 준비

- Spring Boot 서버가 `http://localhost:8080`에서 실행 중이어야 합니다.
- MySQL 컨테이너가 실행 중이어야 합니다.

## 테스트 목록

- `scripts/smoke.js`: 회원가입, 로그인, 내 정보 조회, 게시글 생성/조회 기본 흐름 확인
- `scripts/auth-flow.js`: 회원가입, 로그인, JWT 인증 API 성능 확인
- `scripts/rest-read.js`: 내 정보 조회, 게시글 목록/상세 조회 성능 확인
- `scripts/posts.js`: 게시글 생성, 상세 조회, 수정, 목록 조회, 삭제 확인
- `scripts/chat-http.js`: 채팅방 생성, 목록 조회, 메시지 조회, 읽음 처리 확인
- `scripts/chat-ws.js`: WebSocket/STOMP 연결, 구독, 메시지 발행/수신 확인
- `scripts/notification-sse.js`: 알림 SSE 연결과 초기 notification 이벤트 수신 확인

## 실행 방법

```bash
k6 run k6/scripts/smoke.js
k6 run k6/scripts/auth-flow.js
k6 run k6/scripts/rest-read.js
k6 run k6/scripts/posts.js
k6 run k6/scripts/chat-http.js
k6 run k6/scripts/chat-ws.js
```

SSE 테스트는 `k6/x/sse` 확장이 필요합니다.

```bash
k6 run k6/scripts/notification-sse.js
```

## 설정 변경

VUS, duration, timeout 값은 각 스크립트 상단의 상수에서 수정합니다.

```js
const TARGET_VUS = 100;
const TEST_DURATION = '1m';
```

## 참고

- 테스트는 실행 중 회원, 게시글, 채팅방 데이터를 생성합니다.
- 반복 실행 시 로컬 DB에 테스트 데이터가 쌓일 수 있습니다.
- 현재 SSE 테스트는 실제 알림 발행이 아니라 구독 성공 시 내려오는 초기 이벤트를 검증합니다.
