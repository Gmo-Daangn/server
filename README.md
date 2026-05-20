# Daangn

당근마켓 클론 코딩 서버

---

## 프로젝트 소개

중고거래 플랫폼인 당근마켓의 클론 코딩 서버입니다.

- 상품 거래 — 게시글 CRUD, 결제
- 채팅 — WebSocket STOMP 기반 1:1, 멀티 채팅
- 알림 — SSE 구독

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 4.0.3 |
| Database | MySQL 9.7 |
| Realtime | WebSocket (STOMP) |
| Infra | Docker, AWS |

## 로컬 실행 방법

### 1. 인프라 실행

```bash
docker compose up -d
```

### 2. 애플리케이션 실행

IDE에서 `SPRING_PROFILES_ACTIVE=dev`로 실행 혹은 아래 명령어 입력

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

| 엔드포인트 | 주소 |
|-----------|------|
| API | `http://localhost:8080` |
| API Docs | `http://localhost:8080/docs/index.html` |

---

## 팀 구성

| 이름 | 도메인 |
|------|--------|
| 강상욱 | 알림, AOP |
| 김재성 | 회원, 인증, 인프라 |
| 차민혁 | 채팅, 상품 |

---

## 문서

| 문서 | 설명 |
|------|------|
| [프로젝트 개요](docs/01-project-overview.md) | 목표, 핵심 기능, 기술 스택, 개발 일정, KPI |
| [ERD](docs/02-erd.md) | 도메인별 테이블 정의 |
| [팀 규칙](docs/03-team-rules.md) | 커밋 컨벤션, 브랜치 전략, 코드 스타일, 패키지 구조 |
| [API 명세서](docs/04-api-spec.md) | 전 도메인 REST API 및 WebSocket 명세 |
