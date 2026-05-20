# GMO당근 API 문서

## 목차

- [회원가입 성공](#_회원가입_성공)
- [로그인 성공](#_로그인_성공)
- [내정보 조회 성공](#_내정보_조회_성공)
- [알림 SSE 구독](#_알림_sse_구독)
- [알림 목록 조회](#_알림_목록_조회)
- [알림 읽음 처리](#_알림_읽음_처리)
- [알림 삭제](#_알림_삭제)
- [채팅방 생성 성공](#_채팅방_생성_성공)
- [채팅방 재입장 성공](#_채팅방_재입장_성공)
- [채팅방 목록 조회 성공](#_채팅방_목록_조회_성공)
- [채팅 메시지 목록 조회 성공](#_채팅_메시지_목록_조회_성공)
- [채팅방 읽음 처리 성공](#_채팅방_읽음_처리_성공)
- [채팅 메시지 수정 성공](#_채팅_메시지_수정_성공)
- [채팅 메시지 삭제 성공](#_채팅_메시지_삭제_성공)

<a id="_회원가입_성공"></a>

## 회원가입 성공

<a id="_요청"></a>

### 요청

```http
POST /api/v1/auth HTTP/1.1
Content-Type: application/json
Accept: application/json
Content-Length: 140
Host: localhost:8080

{"email":"test@test.com","nickname":"이름","password":"password","address":{"city":"서울시","district":"동작구","town":"사당동"}}
```

| Path       | Type     | Description |
|------------|----------|-------------|
| `email`    | `String` | 이메일         |
| `password` | `String` | 비밀번호        |
| `nickname` | `String` | 닉네임         |
| `address`  | `Object` | 주소 정보       |

<a id="_응답"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 88

{"code":200,"localDateTime":"2026-05-19T15:40:56.171067","message":"정상","data":null}
```

| Path            | Type     | Description      |
|-----------------|----------|------------------|
| `code`          | `Number` | HTTP Status Code |
| `localDateTime` | `String` | 시간               |
| `message`       | `String` | 설명 메시지           |
| `data`          | `Null`   | 데이터              |

<a id="_curl"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/auth' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json' \
    -d '{"email":"test@test.com","nickname":"이름","password":"password","address":{"city":"서울시","district":"동작구","town":"사당동"}}'
```

<a id="_로그인_성공"></a>

## 로그인 성공

<a id="_요청_2"></a>

### 요청

```http
POST /api/v1/auth/login HTTP/1.1
Content-Type: application/json
Accept: application/json
Content-Length: 47
Host: localhost:8080

{"email":"test@test.com","password":"password"}
```

| Path       | Type     | Description |
|------------|----------|-------------|
| `email`    | `String` | 이메일         |
| `password` | `String` | 비밀번호        |

<a id="_응답_2"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 331

{"code":200,"localDateTime":"2026-05-19T15:41:04.209837","message":"정상","data":{"grantType":"Bearer","accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiYXV0aCI6IlJPTEVfTUVNQkVSLEZBQ1RPUl9QQVNTV09SRCIsIm1lbWJlcklkIjoxLCJpYXQiOjE3NzkxNzI4NjQsImV4cCI6MTc3OTE3NDY2NH0.TZftY3IXUnfZmDpmUJUJSU3yXmENXrQvk_RlSmHvDOc"}}
```

| Path               | Type     | Description      |
|--------------------|----------|------------------|
| `code`             | `Number` | HTTP Status Code |
| `localDateTime`    | `String` | 시간               |
| `message`          | `String` | 설명 메시지           |
| `data.grantType`   | `String` | 토큰 타입            |
| `data.accessToken` | `String` | 접근 토큰            |

<a id="_curl_2"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/auth/login' -i -X POST \
    -H 'Content-Type: application/json' \
    -H 'Accept: application/json' \
    -d '{"email":"test@test.com","password":"password"}'
```

<a id="_내정보_조회_성공"></a>

## 내정보 조회 성공

<a id="_요청_3"></a>

### 요청

```http
GET /api/v1/members HTTP/1.1
Authorization: Bearer {accessToken}
Host: localhost:8080
```

| Name            | Description   |
|-----------------|---------------|
| `Authorization` | Bearer 액세스 토큰 |

<a id="_응답_3"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 196

{"code":200,"localDateTime":"2026-05-19T15:41:17.176479","message":"정상","data":{"email":"test@test.com","nickname":"테스트","address":{"city":"서울","district":"강남","town":"역삼"}}}
```

| Path                    | Type     | Description |
|-------------------------|----------|-------------|
| `code`                  | `Number` | HTTP 상태 코드  |
| `localDateTime`         | `String` | 응답시간        |
| `message`               | `String` | 응답 메시지      |
| `data.email`            | `String` | 이메일         |
| `data.nickname`         | `String` | 닉네임         |
| `data.address.city`     | `String` | 시/도         |
| `data.address.district` | `String` | 구/군         |
| `data.address.town`     | `String` | 동/읍/면       |

<a id="_curl_3"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/members' -i -X GET \
    -H 'Authorization: Bearer {accessToken}'
```

<a id="_알림_sse_구독"></a>

## 알림 SSE 구독

<a id="_요청_4"></a>

### 요청

```http
GET /api/v1/notification/sse?memberId=1 HTTP/1.1
Accept: text/event-stream
Host: localhost:8080
```

| Parameter  | Description |
|------------|-------------|
| `memberId` | 구독할 멤버 ID   |

<a id="_응답_4"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: text/event-stream
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
```

<a id="_curl_4"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/notification/sse?memberId=1' -i -X GET \
    -H 'Accept: text/event-stream'
```

<a id="_알림_목록_조회"></a>

## 알림 목록 조회

<a id="_요청_5"></a>

### 요청

```http
GET /api/v1/notification?memberId=1 HTTP/1.1
Accept: application/json
Host: localhost:8080
```

| Parameter  | Description |
|------------|-------------|
| `memberId` | 수신자(회원) ID  |

<a id="_응답_5"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 267

{"code":200,"localDateTime":"2026-05-19T15:41:17.441297","message":"정상","data":[{"id":1,"receiverId":1,"templateType":"CHAT","templateTitle":"채팅","identifier":1,"message":"새 채팅: 안녕하세요","isRead":false,"createdAt":"2026-05-19T15:41:17.439812"}]}
```

| Path                   | Type      | Description         |
|------------------------|-----------|---------------------|
| `code`                 | `Number`  | HTTP 상태 코드          |
| `localDateTime`        | `String`  | 응답 시간               |
| `message`              | `String`  | 응답 메시지              |
| `data[].id`            | `Number`  | 알림 ID               |
| `data[].receiverId`    | `Number`  | 수신자 회원 ID           |
| `data[].templateType`  | `String`  | 알림 템플릿 타입           |
| `data[].templateTitle` | `String`  | 알림 제목               |
| `data[].identifier`    | `Number`  | 도메인 식별자 (예: 채팅방 ID) |
| `data[].message`       | `String`  | 알림 본문 내용            |
| `data[].isRead`        | `Boolean` | 읽음 여부               |
| `data[].createdAt`     | `String`  | 알림 생성 시간            |

<a id="_curl_5"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/notification?memberId=1' -i -X GET \
    -H 'Accept: application/json'
```

<a id="_알림_읽음_처리"></a>

## 알림 읽음 처리

<a id="_요청_6"></a>

### 요청

```http
PATCH /api/v1/notification/1 HTTP/1.1
Accept: application/json
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
```

**Table 1. /api/v1/notification/{id}**

| Parameter | Description  |
|-----------|--------------|
| `id`      | 읽음 처리할 알림 ID |

<a id="_응답_6"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 113

{"code":200,"localDateTime":"2026-05-19T15:41:17.452841","message":"정상","data":"알림 읽음 처리 성공"}
```

| Path            | Type     | Description |
|-----------------|----------|-------------|
| `code`          | `Number` | HTTP 상태 코드  |
| `localDateTime` | `String` | 응답 시간       |
| `message`       | `String` | 응답 메시지      |
| `data`          | `String` | 결과 텍스트 데이터  |

<a id="_curl_6"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/notification/1' -i -X PATCH \
    -H 'Accept: application/json'
```

<a id="_알림_삭제"></a>

## 알림 삭제

<a id="_요청_7"></a>

### 요청

```http
DELETE /api/v1/notification/1 HTTP/1.1
Accept: application/json
Host: localhost:8080
```

**Table 2. /api/v1/notification/{id}**

| Parameter | Description |
|-----------|-------------|
| `id`      | 삭제할 알림 ID   |

<a id="_응답_7"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 106

{"code":200,"localDateTime":"2026-05-19T15:41:17.460935","message":"정상","data":"알림 삭제 성공"}
```

| Path            | Type     | Description |
|-----------------|----------|-------------|
| `code`          | `Number` | HTTP 상태 코드  |
| `localDateTime` | `String` | 응답 시간       |
| `message`       | `String` | 응답 메시지      |
| `data`          | `String` | 결과 텍스트 데이터  |

<a id="_curl_7"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/notification/1' -i -X DELETE \
    -H 'Accept: application/json'
```

<a id="_채팅방_생성_성공"></a>

## 채팅방 생성 성공

<a id="_요청_8"></a>

### 요청

```http
POST /api/v1/chat/rooms/enter HTTP/1.1
Content-Type: application/json
Content-Length: 63
Host: localhost:8080

{
  "memberId": 1,
  "targetMemberId": 2,
  "productId": 100
}
```

| Path             | Type     | Description        |
|------------------|----------|--------------------|
| `memberId`       | `Number` | 채팅방에 입장하는 회원 ID    |
| `targetMemberId` | `Number` | 1대1 채팅 상대 회원 ID    |
| `productId`      | `Number` | 상품 기반 채팅일 경우 상품 ID |

<a id="_응답_8"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 147

{"code":200,"localDateTime":"2026-05-19T15:41:15.711433","message":"정상","data":{"roomId":1,"created":true,"message":"채팅방 생성 성공"}}
```

| Path            | Type      | Description   |
|-----------------|-----------|---------------|
| `code`          | `Number`  | HTTP 상태 코드    |
| `localDateTime` | `String`  | 응답 시간         |
| `message`       | `String`  | 응답 메시지        |
| `data.roomId`   | `Number`  | 채팅방 ID        |
| `data.created`  | `Boolean` | 새 채팅방 생성 여부   |
| `data.message`  | `String`  | 채팅방 입장 결과 메시지 |

<a id="_curl_8"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/rooms/enter' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "memberId": 1,
  "targetMemberId": 2,
  "productId": 100
}
'
```

<a id="_채팅방_재입장_성공"></a>

## 채팅방 재입장 성공

<a id="_요청_9"></a>

### 요청

```http
POST /api/v1/chat/rooms/enter HTTP/1.1
Content-Type: application/json
Content-Length: 63
Host: localhost:8080

{
  "memberId": 1,
  "targetMemberId": 2,
  "productId": 100
}
```

| Path             | Type     | Description        |
|------------------|----------|--------------------|
| `memberId`       | `Number` | 채팅방에 입장하는 회원 ID    |
| `targetMemberId` | `Number` | 1대1 채팅 상대 회원 ID    |
| `productId`      | `Number` | 상품 기반 채팅일 경우 상품 ID |

<a id="_응답_9"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 137

{"code":200,"localDateTime":"2026-05-19T15:41:16.48059","message":"정상","data":{"roomId":1,"created":false,"message":"입장 성공"}}
```

| Path            | Type      | Description   |
|-----------------|-----------|---------------|
| `code`          | `Number`  | HTTP 상태 코드    |
| `localDateTime` | `String`  | 응답 시간         |
| `message`       | `String`  | 응답 메시지        |
| `data.roomId`   | `Number`  | 기존 채팅방 ID     |
| `data.created`  | `Boolean` | 새 채팅방 생성 여부   |
| `data.message`  | `String`  | 채팅방 입장 결과 메시지 |

<a id="_curl_9"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/rooms/enter' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "memberId": 1,
  "targetMemberId": 2,
  "productId": 100
}
'
```

<a id="_채팅방_목록_조회_성공"></a>

## 채팅방 목록 조회 성공

<a id="_요청_10"></a>

### 요청

```http
GET /api/v1/chat/rooms?memberId=1 HTTP/1.1
Host: localhost:8080
```

| Parameter  | Description        |
|------------|--------------------|
| `memberId` | 채팅방 목록을 조회하는 회원 ID |

<a id="_응답_10"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 274

{"code":200,"localDateTime":"2026-05-19T15:41:16.461702","message":"정상","data":[{"roomId":1,"productId":100,"otherMemberId":2,"otherMemberNickname":"receiver","lastMessage":"hello integration","lastMessageCreatedAt":"2026-05-19T15:41:16.367673","unreadMessageCount":0}]}
```

| Path                          | Type     | Description    |
|-------------------------------|----------|----------------|
| `code`                        | `Number` | HTTP 상태 코드     |
| `localDateTime`               | `String` | 응답 시간          |
| `message`                     | `String` | 응답 메시지         |
| `data[].roomId`               | `Number` | 채팅방 ID         |
| `data[].productId`            | `Number` | 채팅방과 연결된 상품 ID |
| `data[].otherMemberId`        | `Number` | 상대 회원 ID       |
| `data[].otherMemberNickname`  | `String` | 상대 회원 닉네임      |
| `data[].lastMessage`          | `String` | 마지막 메시지 내용     |
| `data[].lastMessageCreatedAt` | `String` | 마지막 메시지 생성 시간  |
| `data[].unreadMessageCount`   | `Number` | 채팅방의 미읽음 메시지 수 |

<a id="_curl_10"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/rooms?memberId=1' -i -X GET
```

<a id="_채팅_메시지_목록_조회_성공"></a>

## 채팅 메시지 목록 조회 성공

<a id="_요청_11"></a>

### 요청

```http
GET /api/v1/chat/messages/1?memberId=1 HTTP/1.1
Host: localhost:8080
```

**Table 3. /api/v1/chat/messages/{roomId}**

| Parameter | Description     |
|-----------|-----------------|
| `roomId`  | 메시지를 조회할 채팅방 ID |

| Parameter  | Description        |
|------------|--------------------|
| `memberId` | 메시지 목록을 조회하는 회원 ID |

<a id="_응답_11"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 243

{"code":200,"localDateTime":"2026-05-19T15:41:16.405028","message":"정상","data":[{"messageId":1,"roomId":1,"senderId":1,"message":"hello integration","edited":false,"deleted":false,"unreadCount":1,"createdAt":"2026-05-19T15:41:16.367673"}]}
```

| Path                 | Type      | Description         |
|----------------------|-----------|---------------------|
| `code`               | `Number`  | HTTP 상태 코드          |
| `localDateTime`      | `String`  | 응답 시간               |
| `message`            | `String`  | 응답 메시지              |
| `data[].messageId`   | `Number`  | 메시지 ID              |
| `data[].roomId`      | `Number`  | 채팅방 ID              |
| `data[].senderId`    | `Number`  | 메시지 작성자 회원 ID       |
| `data[].message`     | `String`  | 메시지 내용              |
| `data[].edited`      | `Boolean` | 메시지 수정 여부           |
| `data[].deleted`     | `Boolean` | 메시지 삭제 여부           |
| `data[].unreadCount` | `Number`  | 메시지를 아직 읽지 않은 참여자 수 |
| `data[].createdAt`   | `String`  | 메시지 생성 시간           |

<a id="_curl_11"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/messages/1?memberId=1' -i -X GET
```

<a id="_채팅방_읽음_처리_성공"></a>

## 채팅방 읽음 처리 성공

<a id="_요청_12"></a>

### 요청

```http
POST /api/v1/chat/rooms/read/1 HTTP/1.1
Content-Type: application/json
Content-Length: 20
Host: localhost:8080

{
  "memberId": 2
}
```

**Table 4. /api/v1/chat/rooms/read/{roomId}**

| Parameter | Description   |
|-----------|---------------|
| `roomId`  | 읽음 처리할 채팅방 ID |

| Path       | Type     | Description    |
|------------|----------|----------------|
| `memberId` | `Number` | 읽음 처리 요청 회원 ID |

<a id="_응답_12"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 130

{"code":200,"localDateTime":"2026-05-19T15:41:16.506573","message":"정상","data":{"roomId":1,"memberId":2,"readMessageCount":1}}
```

| Path                    | Type     | Description    |
|-------------------------|----------|----------------|
| `code`                  | `Number` | HTTP 상태 코드     |
| `localDateTime`         | `String` | 응답 시간          |
| `message`               | `String` | 응답 메시지         |
| `data.roomId`           | `Number` | 읽음 처리된 채팅방 ID  |
| `data.memberId`         | `Number` | 읽음 처리 요청 회원 ID |
| `data.readMessageCount` | `Number` | 읽음 처리된 메시지 수   |

<a id="_curl_12"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/rooms/read/1' -i -X POST \
    -H 'Content-Type: application/json' \
    -d '{
  "memberId": 2
}
'
```

<a id="_채팅_메시지_수정_성공"></a>

## 채팅 메시지 수정 성공

<a id="_요청_13"></a>

### 요청

```http
PATCH /api/v1/chat/messages/1 HTTP/1.1
Content-Type: application/json
Content-Length: 55
Host: localhost:8080

{
  "memberId": 1,
  "message": "edited integration"
}
```

**Table 5. /api/v1/chat/messages/{messageId}**

| Parameter   | Description |
|-------------|-------------|
| `messageId` | 수정할 메시지 ID  |

| Path       | Type     | Description     |
|------------|----------|-----------------|
| `memberId` | `Number` | 메시지 수정 요청 회원 ID |
| `message`  | `String` | 수정할 메시지 내용      |

<a id="_응답_13"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 241

{"code":200,"localDateTime":"2026-05-19T15:41:16.536056","message":"정상","data":{"messageId":1,"roomId":1,"senderId":1,"message":"edited integration","edited":true,"deleted":false,"unreadCount":0,"createdAt":"2026-05-19T15:41:16.367673"}}
```

| Path               | Type      | Description         |
|--------------------|-----------|---------------------|
| `code`             | `Number`  | HTTP 상태 코드          |
| `localDateTime`    | `String`  | 응답 시간               |
| `message`          | `String`  | 응답 메시지              |
| `data.messageId`   | `Number`  | 메시지 ID              |
| `data.roomId`      | `Number`  | 채팅방 ID              |
| `data.senderId`    | `Number`  | 메시지 작성자 회원 ID       |
| `data.message`     | `String`  | 수정된 메시지 내용          |
| `data.edited`      | `Boolean` | 메시지 수정 여부           |
| `data.deleted`     | `Boolean` | 메시지 삭제 여부           |
| `data.unreadCount` | `Number`  | 메시지를 아직 읽지 않은 참여자 수 |
| `data.createdAt`   | `String`  | 메시지 생성 시간           |

<a id="_curl_13"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/messages/1' -i -X PATCH \
    -H 'Content-Type: application/json' \
    -d '{
  "memberId": 1,
  "message": "edited integration"
}
'
```

<a id="_채팅_메시지_삭제_성공"></a>

## 채팅 메시지 삭제 성공

<a id="_요청_14"></a>

### 요청

```http
DELETE /api/v1/chat/messages/1 HTTP/1.1
Content-Type: application/json
Content-Length: 20
Host: localhost:8080

{
  "memberId": 1
}
```

**Table 6. /api/v1/chat/messages/{messageId}**

| Parameter   | Description |
|-------------|-------------|
| `messageId` | 삭제할 메시지 ID  |

| Path       | Type     | Description     |
|------------|----------|-----------------|
| `memberId` | `Number` | 메시지 삭제 요청 회원 ID |

<a id="_응답_14"></a>

### 응답

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 251

{"code":200,"localDateTime":"2026-05-19T15:41:16.554984","message":"정상","data":{"messageId":1,"roomId":1,"senderId":1,"message":"삭제된 메시지입니다.","edited":true,"deleted":true,"unreadCount":0,"createdAt":"2026-05-19T15:41:16.367673"}}
```

| Path               | Type      | Description         |
|--------------------|-----------|---------------------|
| `code`             | `Number`  | HTTP 상태 코드          |
| `localDateTime`    | `String`  | 응답 시간               |
| `message`          | `String`  | 응답 메시지              |
| `data.messageId`   | `Number`  | 메시지 ID              |
| `data.roomId`      | `Number`  | 채팅방 ID              |
| `data.senderId`    | `Number`  | 메시지 작성자 회원 ID       |
| `data.message`     | `String`  | 삭제 처리 후 메시지 내용      |
| `data.edited`      | `Boolean` | 메시지 수정 여부           |
| `data.deleted`     | `Boolean` | 메시지 삭제 여부           |
| `data.unreadCount` | `Number`  | 메시지를 아직 읽지 않은 참여자 수 |
| `data.createdAt`   | `String`  | 메시지 생성 시간           |

<a id="_curl_14"></a>

### CURL

```bash
$ curl 'http://localhost:8080/api/v1/chat/messages/1' -i -X DELETE \
    -H 'Content-Type: application/json' \
    -d '{
  "memberId": 1
}
'
```

---
Version 0.0.1-SNAPSHOT
Last updated 2026-05-19 15:30:09 +0900
