# 컨벤션

# Code Convention

**URL 규칙**

| 기능 | HTTP 메서드 | URL | Controller 메서드 |
| --- | --- | --- | --- |
| 목록 | GET | `/posts` | `list()` |
| 상세 | GET | `/posts/{id}` | `detail()` |
| 등록 | POST | `/posts/new` | `create()` |
| 수정 | PATCH | `/posts/{id}` | `edit()` |
| 삭제 | DELETE | `/posts/{id}` | `delete()` |
- 리소스를 식별하여  행위는 메서드로 분리한다.
- 단 다른 메서드로 처리하기 애매한 다음과 같은 경우 POST 사용 가능하다
    - JSON으로 조회 데이터를 넘겨야하는데, GET 메서드를 사용하기 어려운 경우
    - 이외 애매한 경우 ( 팀원과 상의 후 사용 )

### **클래스 네이밍**

| 유형 | 네이밍 규칙 | 예시 |
| --- | --- | --- |
| Controller | `*Controller` | `PostController`, `MemberController` |
| Service | `*Service` | `PostService`, `MemberService` |
| Repository | `*Repository` | `PostRepository`, `MemberRepository` |
| Entity | 단수형 명사 | `Post`, `Member`, `Comment` |
| DTO | `*Dto` ,  `*Request*` ,  `*Response*`  | `PostRequestDto`, `MemberResponseDto` |
- 파스칼 케이스 사용
- 필요에 따라 명사 + 동사 + 유형을 조합하여 사용
    - ex) ChatMessageController, ChatMessageCreateDto

### **메서드 네이밍**

| 기능 | Controller | Service | Repository |
| --- | --- | --- | --- |
| 목록 조회 | `list()` | `getAll()` | `findAll()` |
| 단일 조회 | `detail()` | `getById()` | `findById()` |
| 등록 폼 | `createForm()` | - | - |
| 등록 | `create()` | `save()` | `save()` |
| 수정 폼 | `editForm()` | - | - |
| 수정 | `edit()` | `update()` | `save()` |
| 삭제 | `delete()` | `delete()` | `deleteById()` |
- 카멜 케이스 사용
- 명사, 동사, 명사 + 동사 형식
    - ex) `list()`, `save()`, `editForm()`
- 조건, 행위에 따라 위 컨벤션 조건을 맞춰서 명사와 동사를 혼용하여 작성
    - 본 컨벤션에서 서비스는 find가 아닌 get을 사용해야 함
    - ex) findRoomByIdOrThrow() → getRoomByIdOrThrow()

# :slhw4nu8hybreryigopq: Git Convention

## Commit, Issue, PR, branch 메시지 컨벤션

### 1. 유형 지정

- 유형은 영어 소문자로 작성하기

| 유형 | 의미 |
| --- | --- |
| `feature` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `style` | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `chore` | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| `design` | CSS 등 사용자 UI 디자인 변경 |
| `comment` | 필요한 주석 추가 및 변경 |
| `rename` | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| `remove` | 파일을 삭제하는 작업만 수행한 경우 |
| `!BREAKING CHANGE` | 커다란 API 변경의 경우 |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |

### 2. 제목 끝에는 `.` 사용 금지

### 3. 제목은 50자 이내로 작성하기

### 4. 최대한 직관적으로 바로 파악할 수 있도록 작성하기

### 5. 만일 여러 항목이 있다면 글머리 기호를 통해서 가독성을 높이기

```
feature: AWS S3 버킷에 이미지 저장 로직 작성
- 비동기 로직으로 처리
```

### 6. 브랜치 네이밍 예시

```jsx
feature/add-signup
- 유형/내용-내용
내용 사이에 띄어쓰기는 - 처리
```

## Issue

- 마일스톤 사용
- 가능한 한 해당 기능의 작업자가 직접 이슈 생성
- 디벨롭 브랜치에서 버그 발생 시 bug 이슈 생성 및 기능 담당자 배정

## Commit

- issue 자동 트래킹을 위해 commit메시지 끝에 ‘#Issue number’ 를 반드시 표기
    - issue number = 이슈 생성 시 자동으로 배정되는 번호 / 아래 사진 참고
        
        ![SCR-20260424-jqob.png](attachment:4558d679-4a36-4d1e-b1bd-7733df8152a4:SCR-20260424-jqob.png)
        
    - 커밋 메시지 예시
        
        ![SCR-20260424-jrla.png](attachment:b996197b-0168-474d-837e-ebd87ea496eb:SCR-20260424-jrla.png)
        

## Pull Request

- 작업자 모두에게 코드리뷰 필수(approve)
- main 브랜치의 PR은 코드리뷰 이후 관리자만 PR허가 가능
- issue 자동 트래킹을 위해 PR메시지 끝에 ‘resolved #issue number’ 를 반드시 표기
    - 예시
        
        ![SCR-20260424-jscx.png](attachment:e8b78344-0490-4065-a8de-10594e083ede:SCR-20260424-jscx.png)
        

### Test Code

- 테스트 코드 작성은 아래 링크의 1번 규칙을 따른다.
    
    https://jamie95.tistory.com/125
    

## Branch 전략
- Git - flow
    - main : 라이브 서버 제품으로 출시되는 브랜치
    - develop : 다음 출시 버전을 대비하여 개발하는 브랜치 (develop → main)
    - feature : 추가 기능 개발 브랜치 (feature → develop)
    - release : 다음 버전 출시를 준비하는 브랜치 (develop → release를 통해 QA 진행 이후 release → main)
    - hotfix : main 브랜치에서 발생한 버그를 수정하는 브랜치