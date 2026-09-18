# 03. API 명세

> 이 문서는 bookhub의 **HTTP 계약**을 정의한다.
> 개념 구조는 [01-domain.md](./01-domain.md)가, 유스케이스와 도메인 규칙은 [02-requirements.md](./02-requirements.md)가 담는다.
>
> **도메인 규칙의 단일 출처는 [02-requirements.md](./02-requirements.md)다.** 이 문서는 규칙을 재서술하지 않고 번호(L-2, P-8, B-4 …)로 참조한다.

---

## 1. 공통 규약

### 1.1 Base path

모든 엔드포인트는 `/api/v1` 접두사를 갖는다. 접두사는 경로에 직접 포함하며 `server.servlet.context-path`로 주입하지 않는다 — 문서의 경로와 코드의 `@RequestMapping` 값이 눈으로 일치해야 추적이 쉽다.

### 1.2 인증

JWT Bearer 토큰을 `Authorization` 헤더로 보낸다.

```
Authorization: Bearer <token>
```

**토큰 클레임**

| 클레임 | 타입 | 설명 |
|--------|------|------|
| `userId` | UUID | 호출자 식별자. 요청 body에 절대 받지 않고 항상 여기서 추출한다 |
| `role` | string | `MEMBER` / `LIBRARIAN` / `ADMIN` |
| `libraryId` | UUID \| null | 사서의 소속 지점. **MEMBER·ADMIN은 `null`** (→ M-6) |

`libraryId`가 nullable인 것은 소속이 선택적이어서가 아니라 역할에 따라 존재 여부가 갈리기 때문이다. P-8 검사는 이 클레임이 `null`이 아닌 경우에만 성립하므로, LIBRARIAN 토큰에 `libraryId`가 없으면 인증 자체를 실패 처리한다.

**인증이 필요 없는 엔드포인트**

| 엔드포인트 | 사유 |
|---|---|
| `POST /api/v1/auth/signup/member` | 가입 전 (UC-01) |
| `POST /api/v1/auth/signup/librarian` | 가입 전 (UC-02) |
| `POST /api/v1/auth/login` | 토큰 발급 자체 (UC-03) |
| `GET /api/v1/libraries` | 사서 가입 시 지점을 골라야 하므로 가입 전 호출된다 |

### 1.3 권한 표기법

P-8의 2층 검사를 표에서 구분하기 위해 **권한**과 **소속 제약**을 별도 열로 둔다.

| 열 | 의미 |
|---|---|
| **권한** | 호출 가능한 역할. **권한은 누적이 아니라 분기하므로(→ P-1) 역할이 정확히 일치해야 한다** — LIBRARIAN 엔드포인트를 ADMIN이 호출할 수 없고 그 반대도 마찬가지다. 단 권한이 `MEMBER`인 엔드포인트는 세 역할 모두 호출할 수 있다 (→ P-2) |
| **소속 제약** `—` | 역할 검사만으로 충분 |
| **소속 제약** (조건 명시) | P-8 2층 검사 대상. 역할이 맞아도 이 조건이 거짓이면 403 |

두 열의 조합은 [02-requirements.md](./02-requirements.md)의 액터별 행위 표와 1:1로 대응한다.

### 1.4 에러 응답

**구조**

`common`에 `BusinessException` 추상 클래스를 두고 `code`, `status`, `message`를 갖게 한다. 도메인별 패키지에 구체 예외 클래스를 정의한다.

| 필드 | 설명 |
|------|------|
| `code` | `{도메인}_{사유}` 형식. **규칙 단위**로 부여한다 |
| `message` | 사람이 읽는 설명 |
| `rule` | 위반한 규칙 번호. **운영 환경에서는 제외하고 로그에만 남긴다** |
| `timestamp` | ISO-8601 |
| `path` | 요청 경로 |

코드를 도메인 단위로만 묶지 않는 이유는, 그렇게 하면 프론트가 `message` 문자열을 파싱해 분기하게 되기 때문이다. 분기 가능한 정보는 코드에 담는다.

```json
{
  "code": "LOAN_LIMIT_EXCEEDED",
  "message": "대출 한도를 초과했습니다",
  "rule": "L-2",
  "timestamp": "2026-09-18T14:30:00Z",
  "path": "/api/v1/loans"
}
```

**HTTP 상태 코드 기준**

| 상태 | 용도 |
|------|------|
| 400 | 요청 형식 오류 (검증 실패) |
| 401 | 인증 실패 |
| 403 | 권한 부족 (역할 불일치, 소속 지점 불일치) |
| 404 | 대상 없음 |
| 409 | 도메인 규칙 위반, 상태 전이 위반, 중복 |

**도메인 규칙 위반에 400을 쓰지 않는다.** 요청 자체는 형식상 올바르고, 현재 시스템 상태가 그 요청을 허용하지 않는 것이다. 같은 요청이 어제는 성공했을 수 있고 내일은 성공할 수 있다 — 이는 클라이언트가 고칠 수 있는 종류의 오류가 아니므로 409가 맞다.

### 1.5 페이징

Spring Data `Pageable` 규약을 따른다.

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| `page` | `0` | 0-based |
| `size` | `20` | 한 페이지 항목 수 |
| `sort` | 엔드포인트별 명시 | `필드,방향` (예: `createdAt,desc`) |

**페이징 응답 공통 구조**

| 필드 | 설명 |
|---|---|
| `content` | 항목 배열 |
| `page` | 현재 페이지 (0-based) |
| `size` | 페이지 크기 |
| `totalElements` | 전체 항목 수 |
| `totalPages` | 전체 페이지 수 |

적용 대상: UC-04, UC-06, UC-15, UC-16

### 1.6 날짜 형식

| 타입 | 형식 | 예 |
|------|------|-----|
| `LocalDate` | `yyyy-MM-dd` | `2026-09-18` |
| `Instant` | ISO-8601 | `2026-09-18T14:30:00Z` |

대출 관련 날짜(`loanedAt`, `dueDate`, `closedAt`)는 모두 `LocalDate`다. 시각 단위의 정밀도가 도메인 판단에 쓰이지 않기 때문이다 — 연체 판정도 날짜 비교다(→ L-9).

### 1.7 응답 공통 원칙

| 원칙 | 내용 |
|------|------|
| `userId` 미수신 | 요청 body에 호출자 ID를 받지 않는다. 토큰에서 추출한다. **단 사서가 대출·반납·연장을 대행할 때는 대상 회원 ID를 body로 받는다** |
| `password` 미노출 | 어떤 응답에도 포함하지 않는다 |
| 연체는 계산 필드 | `status`에는 `LOANED` / `RETURNED` / `LOST`만 나간다. 연체 여부는 `isOverdue`, `overdueDays`로 따로 담는다 (→ L-9) |
| 삭제 없음 | `DELETE` 엔드포인트를 두지 않는다. 복본 폐기·계정 탈퇴는 1차 범위 밖이다 |

---

## 2. 엔드포인트 목록

`대응 UC` 열의 **보조**는 다른 UC의 사전조건을 충족시키기 위한 조회를 뜻한다.

### A. 인증 / 회원

| 메서드 | 경로 | 권한 | 소속 제약 | 설명 | 대응 UC |
|--------|------|------|----------|------|---------|
| POST | `/api/v1/auth/signup/member` | 없음 | — | 일반 회원가입 | UC-01 |
| POST | `/api/v1/auth/signup/librarian` | 없음 | — | 사서 가입 신청 (소속 지점 지정) | UC-02 |
| POST | `/api/v1/auth/login` | 없음 | — | 로그인, JWT 발급 | UC-03 |
| GET | `/api/v1/librarians/pending` | ADMIN | — | 승인 대기 사서 목록 | UC-04 |
| POST | `/api/v1/librarians/{id}/approval` | ADMIN | — | 사서 승인 | UC-05 |

### B. 도서 (카탈로그)

| 메서드 | 경로 | 권한 | 소속 제약 | 설명 | 대응 UC |
|--------|------|------|----------|------|---------|
| GET | `/api/v1/books` | MEMBER | — | 도서 검색 (→ [4장](#4-도서-검색-api-uc-06)) | UC-06 |
| GET | `/api/v1/books/{id}` | MEMBER | — | 도서 상세 + 지점별 복본 현황 | UC-07 |
| POST | `/api/v1/books` | LIBRARIAN | **없음** | 서지정보 등록. 전 지점 공통 카탈로그이므로 소속 무관 (→ P-9) | UC-08 |

### C. 복본

| 메서드 | 경로 | 권한 | 소속 제약 | 설명 | 대응 UC |
|--------|------|------|----------|------|---------|
| POST | `/api/v1/book-copies` | LIBRARIAN | 등록 대상 지점 == `libraryId` | 복본 등록 | UC-09 |
| POST | `/api/v1/book-copies/{id}/damage-report` | LIBRARIAN | 복본의 지점 == `libraryId` | 파손 처리 | UC-10 |
| POST | `/api/v1/book-copies/{id}/repair` | LIBRARIAN | 복본의 지점 == `libraryId` | 수선 완료 처리 | UC-10-1 |
| POST | `/api/v1/book-copies/{id}/lost-report` | LIBRARIAN | 복본의 지점 == `libraryId` | 분실 처리 | UC-10-2 |

세 상태 변경을 하나의 `PATCH .../status`로 합치지 않은 이유는 진입 조건이 서로 다르기 때문이다(→ B-1, B-4). 파손은 `AVAILABLE`에서만, 수선은 `DAMAGED`에서만, 분실은 `AVAILABLE`과 `LOANED` 양쪽에서 가능하다. 하나로 합치면 이 분기가 전부 한 엔드포인트의 에러 조건으로 쏟아진다.

### D. 대출

| 메서드 | 경로 | 권한 | 소속 제약 | 설명 | 대응 UC |
|--------|------|------|----------|------|---------|
| POST | `/api/v1/loans` | LIBRARIAN | 복본의 지점 == `libraryId` | 대출 처리 | UC-11 |
| POST | `/api/v1/loans/{id}/return` | LIBRARIAN | 복본의 지점 == `libraryId` | 반납 처리 | UC-12 |
| POST | `/api/v1/loans/{id}/extensions` | MEMBER | LIBRARIAN 호출 시 복본의 지점 == `libraryId` | 대출 연장 | **UC-13, UC-14** |
| GET | `/api/v1/loans/me` | MEMBER | — | 본인 대출 이력 | UC-15 |
| GET | `/api/v1/loans` | LIBRARIAN | 자기 지점 대출만 | 지점 대출 현황 | UC-16 |

> **UC-13 + UC-14 통합 사유**: 상태 전이(`dueDate` 갱신)와 검증 조건(L-4, L-5)이 동일하고, 차이는 소유권 검증(L-8) 유무뿐이다. 호출자 역할은 토큰으로 판별 가능하므로 URL을 나누지 않는다.

반납은 `/return`(단수), 연장은 `/extensions`(복수)다. 반납은 대출당 한 번뿐인 종결 행위이고, 연장은 최대 2회까지 누적되는 사건이기 때문이다(→ L-4).

### E. 운영 / 마스터 데이터

| 메서드 | 경로 | 권한 | 소속 제약 | 설명 | 대응 UC |
|--------|------|------|----------|------|---------|
| POST | `/api/v1/libraries` | ADMIN | — | 지점 개설 | UC-17 |
| PATCH | `/api/v1/libraries/{id}/contact` | LIBRARIAN | 대상 지점 == `libraryId` | 연락 정보(주소·전화) 수정 (→ P-5) | UC-18 |
| PATCH | `/api/v1/libraries/{id}/name` | ADMIN | — | 지점명 수정 (→ P-5) | UC-18-1 |
| POST | `/api/v1/categories` | ADMIN | — | 카테고리 생성 | UC-19 |
| PATCH | `/api/v1/categories/{id}` | ADMIN | — | 카테고리 수정 | UC-20 |
| GET | `/api/v1/libraries` | 없음 | — | 지점 목록 | **보조** — UC-02에서 소속 지점을 고르려면 필요. 인증 전 호출 |
| GET | `/api/v1/categories` | MEMBER | — | 카테고리 계층 목록 | **보조** — UC-08의 분류 선택, UC-06의 필터 선택에 필요 |

지점 정보 수정을 `/contact`와 `/name`으로 나눈 이유는 **필드별로 권한이 갈리기 때문이다**(→ P-5). 한 핸들러에 뭉치면 어떤 필드 조합이 어떤 역할에 허용되는지 코드를 읽어야 알 수 있다. 경로를 나누면 권한 경계가 API 표면에 드러난다 — 복본 상태 변경을 `damage-report` / `repair` / `lost-report`로 나눈 것과 같은 판단이다.

`DELETE` 엔드포인트는 없다. 총 **24개** (23 UC − 1 통합 + 2 보조).

---

## 3. 엔드포인트 상세

### 일반 회원가입 · `POST /api/v1/auth/signup/member` · UC-01

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `email` | string | 이메일 형식, 중복 불가 (→ M-4) |
| `password` | string | 8자 이상 |
| `name` | string | 1~50자 |

```json
{ "email": "reader@example.com", "password": "pass1234", "name": "김독자" }
```

**응답** — `201 Created`

```json
{ "id": "3f9a...", "email": "reader@example.com", "name": "김독자", "role": "MEMBER", "status": "ACTIVE" }
```

가입 즉시 `ACTIVE`다(→ M-1). `password`는 응답에 포함하지 않는다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `USER_EMAIL_DUPLICATED` | 409 | M-4 | 이미 등록된 이메일 |
| `VALIDATION_FAILED` | 400 | — | 형식 위반 |

---

### 사서 가입 신청 · `POST /api/v1/auth/signup/librarian` · UC-02

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `email` | string | 이메일 형식, 중복 불가 (→ M-4) |
| `password` | string | 8자 이상 |
| `name` | string | 1~50자 |
| `libraryId` | UUID | **필수**. 소속 지점 (→ M-6) |

```json
{ "email": "staff@example.com", "password": "pass1234", "name": "박사서", "libraryId": "a1b2..." }
```

**응답** — `201 Created`

```json
{ "id": "7c2d...", "email": "staff@example.com", "name": "박사서", "role": "LIBRARIAN", "status": "PENDING", "library": { "id": "a1b2...", "name": "중앙도서관" } }
```

`PENDING`으로 생성되며 승인 전까지 로그인할 수 없다(→ M-2, M-3).

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `USER_EMAIL_DUPLICATED` | 409 | M-4 | 이미 등록된 이메일 |
| `LIBRARY_NOT_FOUND` | 404 | — | `libraryId`에 해당하는 지점 없음 |
| `VALIDATION_FAILED` | 400 | M-6 | `libraryId` 누락 또는 형식 위반 |

---

### 로그인 · `POST /api/v1/auth/login` · UC-03

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `email` | string | — |
| `password` | string | — |

```json
{ "email": "staff@example.com", "password": "pass1234" }
```

**응답** — `200 OK`

```json
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "user": { "id": "7c2d...", "name": "박사서", "role": "LIBRARIAN", "libraryId": "a1b2..." } }
```

`user.libraryId`는 MEMBER·ADMIN에서 `null`이다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `AUTH_INVALID_CREDENTIALS` | 401 | — | 이메일 없음 또는 비밀번호 불일치 |
| `AUTH_ACCOUNT_PENDING` | 403 | M-3 | 계정이 `PENDING` |

자격 증명 오류는 "이메일 없음"과 "비밀번호 틀림"을 구분하지 않는다. 구분하면 이메일 존재 여부를 탐색할 수 있다.

---

### 승인 대기 사서 목록 · `GET /api/v1/librarians/pending` · UC-04

**요청** — query

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| `page` / `size` | `0` / `20` | 페이징 |
| `sort` | `createdAt,asc` | 신청 순 |

```
GET /api/v1/librarians/pending?page=0&size=20
```

**응답** — `200 OK`

```json
{ "content": [ { "id": "7c2d...", "email": "staff@example.com", "name": "박사서", "library": { "id": "a1b2...", "name": "중앙도서관" }, "createdAt": "2026-09-18T09:00:00Z" } ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```

승인 판단에 소속 지점이 필요하므로(UC-05) 함께 담는다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-4 | ADMIN이 아님 |

---

### 사서 승인 · `POST /api/v1/librarians/{id}/approval` · UC-05

**요청** — path `id` (대상 사서 UUID). body 없음.

```
POST /api/v1/librarians/7c2d.../approval
```

**응답** — `200 OK`

```json
{ "id": "7c2d...", "name": "박사서", "role": "LIBRARIAN", "status": "ACTIVE", "library": { "id": "a1b2...", "name": "중앙도서관" } }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-4 | ADMIN이 아님 |
| `USER_NOT_FOUND` | 404 | — | 대상 없음 |
| `USER_NOT_PENDING` | 409 | M-2 | 이미 `ACTIVE`이거나 사서가 아님 |

---

### 도서 검색 · `GET /api/v1/books` · UC-06

→ [4장](#4-도서-검색-api-uc-06)에서 상세히 다룬다.

---

### 도서 상세 조회 · `GET /api/v1/books/{id}` · UC-07

**요청** — path `id` (Book UUID)

```
GET /api/v1/books/9e4f...
```

**응답** — `200 OK`

```json
{
  "id": "9e4f...", "isbn": "9788983920683", "title": "노르웨이의 숲",
  "author": "무라카미 하루키", "publisher": "민음사", "publishedAt": "2013-09-01",
  "description": null,
  "category": { "id": "c1...", "name": "일본소설", "path": ["문학", "일본소설"] },
  "copiesByLibrary": [
    { "library": { "id": "a1b2...", "name": "중앙도서관" }, "totalCopies": 3, "availableCopies": 1 },
    { "library": { "id": "b3c4...", "name": "동부분관" }, "totalCopies": 2, "availableCopies": 2 }
  ]
}
```

지점별 보유·대출가능 수는 **여기서만** 제공한다. 검색 결과(UC-06)는 집계 스칼라만 담는다 — 근거는 [4.4절](#44-응답에-지점별-내역을-담지-않는-이유).

`availableCopies`는 `AVAILABLE` 복본 수다. `DAMAGED`·`LOST`는 `totalCopies`에 포함되지만 `availableCopies`에서는 빠진다(→ L-10).

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `BOOK_NOT_FOUND` | 404 | — | 대상 없음 |

---

### 도서 등록 · `POST /api/v1/books` · UC-08

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `isbn` | string | 중복 불가 |
| `title` | string | 필수 |
| `author` | string | 필수 |
| `publisher` | string | 필수 |
| `publishedAt` | LocalDate | `yyyy-MM-dd` |
| `categoryId` | UUID | 필수 |
| `description` | string? | 선택 |

```json
{ "isbn": "9788983920683", "title": "노르웨이의 숲", "author": "무라카미 하루키", "publisher": "민음사", "publishedAt": "2013-09-01", "categoryId": "c1..." }
```

**응답** — `201 Created`

```json
{ "id": "9e4f...", "isbn": "9788983920683", "title": "노르웨이의 숲", "category": { "id": "c1...", "name": "일본소설" }, "totalCopies": 0 }
```

서지정보만 생기고 실물은 아직 없다. `totalCopies`가 0인 것이 정상이다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `BOOK_ISBN_DUPLICATED` | 409 | — | 이미 등록된 ISBN |
| `CATEGORY_NOT_FOUND` | 404 | — | 분류 없음 |

**소속 지점을 검사하지 않는다** (→ P-9). Book은 전 지점 공통 카탈로그이므로 어느 지점 사서든 등록할 수 있다.

---

### 복본 등록 · `POST /api/v1/book-copies` · UC-09

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `bookId` | UUID | 필수 |
| `libraryId` | UUID | **토큰의 `libraryId`와 일치해야 함** (→ P-8) |
| `callNumber` | string | 지점 내 유일 |

```json
{ "bookId": "9e4f...", "libraryId": "a1b2...", "callNumber": "833.6-무라카-3" }
```

**응답** — `201 Created`

```json
{ "id": "d5e6...", "callNumber": "833.6-무라카-3", "status": "AVAILABLE", "book": { "id": "9e4f...", "title": "노르웨이의 숲" }, "library": { "id": "a1b2...", "name": "중앙도서관" } }
```

`AVAILABLE`로 시작한다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | `libraryId` ≠ 토큰의 소속 |
| `BOOK_NOT_FOUND` | 404 | — | 서지정보 없음 |
| `BOOK_COPY_CALL_NUMBER_DUPLICATED` | 409 | — | 같은 지점에 동일 청구기호 존재 |

---

### 복본 파손 처리 · `POST /api/v1/book-copies/{id}/damage-report` · UC-10

**요청** — path `id`. body 없음.

```
POST /api/v1/book-copies/d5e6.../damage-report
```

**응답** — `200 OK`

```json
{ "id": "d5e6...", "callNumber": "833.6-무라카-3", "status": "DAMAGED" }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | 복본의 지점 ≠ 토큰의 소속 |
| `BOOK_COPY_NOT_FOUND` | 404 | — | 대상 없음 |
| `BOOK_COPY_LOANED_CANNOT_DAMAGE` | 409 | **B-4** | 복본이 `LOANED` — 먼저 반납해야 한다 |
| `BOOK_COPY_INVALID_TRANSITION` | 409 | B-1 | 복본이 `DAMAGED` 또는 `LOST` |

`LOANED` 상태를 별도 코드로 뺀 이유는 클라이언트가 "반납을 먼저 하세요"라는 다음 행동을 안내할 수 있어야 하기 때문이다. 나머지 불가 상태는 안내할 다음 행동이 없다.

---

### 복본 수선 완료 처리 · `POST /api/v1/book-copies/{id}/repair` · UC-10-1

**요청** — path `id`. body 없음.

```
POST /api/v1/book-copies/d5e6.../repair
```

**응답** — `200 OK`

```json
{ "id": "d5e6...", "callNumber": "833.6-무라카-3", "status": "AVAILABLE" }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | 복본의 지점 ≠ 토큰의 소속 |
| `BOOK_COPY_NOT_FOUND` | 404 | — | 대상 없음 |
| `BOOK_COPY_NOT_DAMAGED` | 409 | B-2 | 복본이 `DAMAGED`가 아님 |

---

### 복본 분실 처리 · `POST /api/v1/book-copies/{id}/lost-report` · UC-10-2

**요청** — path `id`. body 없음.

```
POST /api/v1/book-copies/d5e6.../lost-report
```

**응답** — `200 OK`

```json
{ "id": "d5e6...", "callNumber": "833.6-무라카-3", "status": "LOST", "closedLoan": { "id": "f7a8...", "status": "LOST", "closedAt": "2026-09-18" } }
```

대출 중이던 복본이면 해당 대출도 함께 종료되므로(→ B-5), 무엇이 종료됐는지 응답에 담는다. 대출 중이 아니었다면 `closedLoan`은 `null`이다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | 복본의 지점 ≠ 토큰의 소속 |
| `BOOK_COPY_NOT_FOUND` | 404 | — | 대상 없음 |
| `BOOK_COPY_INVALID_TRANSITION` | 409 | B-1, B-3 | 복본이 `DAMAGED` 또는 이미 `LOST` |

파손과 달리 `LOANED`에서도 호출할 수 있다(→ B-4). 분실된 책은 반납 자체가 불가능하기 때문이다.

---

### 대출 처리 · `POST /api/v1/loans` · UC-11

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `memberId` | UUID | 대출받을 회원. **대행 처리이므로 body로 받는다** |
| `bookCopyId` | UUID | 복본의 지점 == 토큰의 `libraryId` |

```json
{ "memberId": "3f9a...", "bookCopyId": "d5e6..." }
```

**응답** — `201 Created`

```json
{
  "id": "f7a8...", "status": "LOANED",
  "loanedAt": "2026-09-18", "dueDate": "2026-10-02", "closedAt": null,
  "extensionCount": 0, "isOverdue": false, "overdueDays": 0,
  "member": { "id": "3f9a...", "name": "김독자" },
  "bookCopy": { "id": "d5e6...", "callNumber": "833.6-무라카-3", "book": { "title": "노르웨이의 숲" } }
}
```

`dueDate`는 대출일 + 14일이다(→ L-1).

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3, L-6 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | **P-8** | 복본의 지점 ≠ 토큰의 소속 |
| `USER_NOT_FOUND` | 404 | — | 회원 없음 |
| `BOOK_COPY_NOT_FOUND` | 404 | — | 복본 없음 |
| `USER_NOT_ACTIVE` | 409 | **L-13** | 회원이 `PENDING` — M-8에 따라 승인 대기 중인 사서 계정이다 |
| `BOOK_COPY_NOT_AVAILABLE` | 409 | **L-10** | 복본이 `AVAILABLE`이 아님 |
| `LOAN_LIMIT_EXCEEDED` | 409 | **L-2** | 진행 중인 대출이 이미 3건 |
| `LOAN_OVERDUE_EXISTS` | 409 | **L-3** | 연체 중인 대출 보유 |

사서가 자기 대출을 직접 처리하는 것은 막지 않는다(→ L-11).

---

### 반납 처리 · `POST /api/v1/loans/{id}/return` · UC-12

**요청** — path `id` (Loan UUID). body 없음.

```
POST /api/v1/loans/f7a8.../return
```

**응답** — `200 OK`

```json
{ "id": "f7a8...", "status": "RETURNED", "loanedAt": "2026-09-18", "dueDate": "2026-10-02", "closedAt": "2026-09-30", "isOverdue": false, "overdueDays": 0, "bookCopy": { "id": "d5e6...", "status": "AVAILABLE" } }
```

`closedAt`에 실제 반납일이 들어간다. 복본은 `AVAILABLE`로 돌아간다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3, L-6 | LIBRARIAN이 아님 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | 복본의 지점 ≠ 토큰의 소속 |
| `LOAN_NOT_FOUND` | 404 | — | 대상 없음 |
| `LOAN_ALREADY_CLOSED` | 409 | L-12 | 이미 `RETURNED` 또는 `LOST` |

---

### 대출 연장 · `POST /api/v1/loans/{id}/extensions` · UC-13, UC-14

**요청** — path `id` (Loan UUID). body 없음.

호출자 역할에 따라 검사가 갈린다.

| 호출자 | 추가 검사 |
|--------|----------|
| MEMBER | 해당 대출이 본인 것인지 확인 (→ L-8) |
| LIBRARIAN | 복본의 지점 == 토큰의 `libraryId` (→ P-8) |

```
POST /api/v1/loans/f7a8.../extensions
```

**응답** — `200 OK`

```json
{ "id": "f7a8...", "status": "LOANED", "dueDate": "2026-10-09", "extensionCount": 1, "remainingExtensions": 1, "isOverdue": false, "overdueDays": 0 }
```

`dueDate`가 7일 연장되고 `extensionCount`가 1 오른다(→ L-4). 새 대출 행이 생기지 않는다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `LOAN_NOT_OWNED` | 403 | **L-8** | MEMBER가 타인의 대출을 연장 시도 |
| `ACCESS_DENIED_LIBRARY` | 403 | P-8 | LIBRARIAN인데 복본의 지점 ≠ 소속 |
| `LOAN_NOT_FOUND` | 404 | — | 대상 없음 |
| `LOAN_ALREADY_CLOSED` | 409 | L-12 | 이미 종료된 대출 |
| `LOAN_EXTENSION_LIMIT_EXCEEDED` | 409 | **L-4** | 이미 2회 연장 |
| `LOAN_OVERDUE_CANNOT_EXTEND` | 409 | **L-5** | 연체 중 |

---

### 본인 대출 이력 조회 · `GET /api/v1/loans/me` · UC-15

**요청** — query

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| `status` | 전체 | `LOANED` / `RETURNED` / `LOST` 필터 |
| `page` / `size` | `0` / `20` | 페이징 |
| `sort` | `loanedAt,desc` | 최근 대출 순 |

대상 회원은 토큰의 `userId`다. body나 query로 받지 않는다.

```
GET /api/v1/loans/me?status=LOANED&page=0&size=20
```

**응답** — `200 OK`

```json
{ "content": [ { "id": "f7a8...", "status": "LOANED", "loanedAt": "2026-09-18", "dueDate": "2026-10-02", "closedAt": null, "extensionCount": 1, "isOverdue": true, "overdueDays": 3, "bookCopy": { "callNumber": "833.6-무라카-3", "book": { "title": "노르웨이의 숲" }, "library": { "name": "중앙도서관" } } } ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```

`status`에는 `OVERDUE`가 없다. 연체는 `isOverdue`·`overdueDays` 계산 필드로 나간다(→ L-9).

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `AUTH_TOKEN_INVALID` | 401 | — | 토큰 없음/무효 |

---

### 지점 대출 현황 조회 · `GET /api/v1/loans` · UC-16

**요청** — query

| 파라미터 | 기본값 | 설명 |
|---|---|---|
| `status` | 전체 | `LOANED` / `RETURNED` / `LOST` 필터 |
| `overdueOnly` | `false` | 연체 건만 |
| `page` / `size` | `0` / `20` | 페이징 |
| `sort` | `dueDate,asc` | 반납 임박 순 |

조회 범위는 토큰의 `libraryId`로 **서버가 결정한다**. 클라이언트가 지점을 지정할 수 없다 — 지정 가능하면 P-8이 쿼리 파라미터로 우회된다.

```
GET /api/v1/loans?overdueOnly=true&page=0&size=20
```

**응답** — `200 OK`

```json
{ "content": [ { "id": "f7a8...", "status": "LOANED", "loanedAt": "2026-09-01", "dueDate": "2026-09-15", "closedAt": null, "isOverdue": true, "overdueDays": 3, "member": { "id": "3f9a...", "name": "김독자" }, "bookCopy": { "callNumber": "833.6-무라카-3", "book": { "title": "노르웨이의 숲" } } } ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
```

`overdueOnly`는 저장된 상태가 아니라 `dueDate` 비교로 평가된다(→ L-9). 정렬 기본값이 `dueDate,asc`인 것은 연체·임박 건이 먼저 보여야 독촉 업무에 쓸 수 있기 때문이다.

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3 | LIBRARIAN이 아님 |
| `AUTH_LIBRARY_CLAIM_MISSING` | 401 | M-6 | LIBRARIAN 토큰에 `libraryId` 없음 |

---

### 지점 개설 · `POST /api/v1/libraries` · UC-17

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `name` | string | 필수 |
| `address` | string | 필수 |
| `phone` | string | 필수 |

```json
{ "name": "서부분관", "address": "서울시 ...", "phone": "02-000-0000" }
```

**응답** — `201 Created`

```json
{ "id": "e9f0...", "name": "서부분관", "address": "서울시 ...", "phone": "02-000-0000" }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | **P-5** | ADMIN이 아님 |

---

### 지점 연락 정보 수정 · `PATCH /api/v1/libraries/{id}/contact` · UC-18

**요청** — path `id`, body (부분 수정)

| 필드 | 타입 | 제약 |
|------|------|------|
| `address` | string? | 선택 |
| `phone` | string? | 선택 |

`name`은 이 엔드포인트로 변경할 수 없다. body에 포함되면 무시하지 않고 `VALIDATION_FAILED`로 거부한다 — 조용히 무시하면 클라이언트가 변경됐다고 오해한다.

```json
{ "phone": "02-111-2222" }
```

**응답** — `200 OK`

```json
{ "id": "a1b2...", "name": "중앙도서관", "address": "서울시 ...", "phone": "02-111-2222" }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-3, **P-5** | LIBRARIAN이 아님 (ADMIN도 호출 불가) |
| `ACCESS_DENIED_LIBRARY` | 403 | **P-8** | 대상 지점 ≠ 토큰의 소속 |
| `LIBRARY_NOT_FOUND` | 404 | — | 대상 없음 |
| `VALIDATION_FAILED` | 400 | P-5 | body에 `name` 포함 |

---

### 지점명 수정 · `PATCH /api/v1/libraries/{id}/name` · UC-18-1

**요청** — path `id`, body

| 필드 | 타입 | 제약 |
|------|------|------|
| `name` | string | 필수 |

```json
{ "name": "중앙도서관(본관)" }
```

**응답** — `200 OK`

```json
{ "id": "a1b2...", "name": "중앙도서관(본관)", "address": "서울시 ...", "phone": "02-111-2222" }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | **P-4, P-5** | ADMIN이 아님 (소속 LIBRARIAN도 호출 불가) |
| `LIBRARY_NOT_FOUND` | 404 | — | 대상 없음 |

소속 제약이 없다. ADMIN은 소속이 없고(→ M-6), P-8은 LIBRARIAN 전용 규칙이므로 이 엔드포인트는 2층 검사 대상이 아니다.

---

### 카테고리 생성 · `POST /api/v1/categories` · UC-19

**요청** — body

| 필드 | 타입 | 제약 |
|------|------|------|
| `name` | string | 필수 |
| `parentId` | UUID? | 생략 시 최상위 분류 |

```json
{ "name": "일본소설", "parentId": "c0..." }
```

**응답** — `201 Created`

```json
{ "id": "c1...", "name": "일본소설", "parent": { "id": "c0...", "name": "문학" }, "path": ["문학", "일본소설"] }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | **P-6** | ADMIN이 아님 |
| `CATEGORY_NOT_FOUND` | 404 | — | `parentId` 대상 없음 |

---

### 카테고리 수정 · `PATCH /api/v1/categories/{id}` · UC-20

**요청** — path `id`, body (부분 수정)

| 필드 | 타입 | 제약 |
|------|------|------|
| `name` | string? | 선택 |
| `parentId` | UUID? | 자기 자신·자신의 하위 지정 불가 (→ C-2) |

```json
{ "parentId": "c9..." }
```

**응답** — `200 OK`

```json
{ "id": "c1...", "name": "일본소설", "parent": { "id": "c9...", "name": "외국문학" }, "path": ["외국문학", "일본소설"] }
```

**에러**

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `ACCESS_DENIED_ROLE` | 403 | P-6 | ADMIN이 아님 |
| `CATEGORY_NOT_FOUND` | 404 | — | 대상 또는 `parentId` 없음 |
| `CATEGORY_CIRCULAR_REFERENCE` | 409 | **C-2** | 자기 자신 또는 자신의 하위를 상위로 지정 |

---

### 지점 목록 · `GET /api/v1/libraries` · 보조 (UC-02)

**요청** — 파라미터 없음. 인증 불필요.

```
GET /api/v1/libraries
```

**응답** — `200 OK`

```json
[ { "id": "a1b2...", "name": "중앙도서관", "address": "서울시 ..." }, { "id": "b3c4...", "name": "동부분관", "address": "서울시 ..." } ]
```

사서 가입(UC-02)에서 소속 지점을 고르려면 가입 전에 호출할 수 있어야 한다. 지점 수가 많지 않아 페이징하지 않는다. M-7이 최소 한 건을 보장하므로 빈 배열은 나오지 않는다.

**에러** — 없음.

---

### 카테고리 계층 목록 · `GET /api/v1/categories` · 보조 (UC-08, UC-06)

**요청** — 파라미터 없음.

```
GET /api/v1/categories
```

**응답** — `200 OK`

```json
[ { "id": "c0...", "name": "문학", "children": [ { "id": "c1...", "name": "일본소설", "children": [] }, { "id": "c2...", "name": "한국소설", "children": [] } ] } ]
```

트리 형태로 반환한다. 도서 등록(UC-08)의 분류 선택과 검색(UC-06)의 카테고리 필터가 모두 계층을 보여줘야 하기 때문이다. C-2가 순환을 막으므로 이 트리는 유한하다.

**지점 목록(`GET /api/v1/libraries`)과 달리 인증을 요구한다.** 지점 목록은 사서 가입(UC-02) 과정에서 소속을 고르기 위해 인증 전에 호출되어야 하지만, 카테고리는 UC-06과 UC-08에서만 쓰이고 두 유스케이스 모두 로그인을 전제하므로 공개할 이유가 없다. 두 보조 엔드포인트의 권한이 다른 것은 실수가 아니라 호출 시점이 다르기 때문이다.

**에러** — 없음.

---

## 4. 도서 검색 API (UC-06)

`GET /api/v1/books`

### 4.1 파라미터

| 구분 | 파라미터 | 타입 | 생략 시 기본 동작 |
|------|---------|------|------------------|
| 검색어 | `keyword` | string | **전체 조회** — 제목·저자·ISBN 어느 하나라도 부분 일치 |
| 필터 | `libraryId` | UUID | **전 지점** |
| 필터 | `categoryId` | UUID | **전 분류**. 지정 시 **하위 카테고리 포함** (→ C-1) |
| 필터 | `availableOnly` | boolean | **`false`** — 대출 불가 도서도 결과에 포함 |
| 페이징 | `page` / `size` | int | `0` / `20` |
| 페이징 | `sort` | string | `title,asc` |

네 조건은 **AND로 결합**한다. 모두 생략하면 전체 도서 목록이 된다.

```
GET /api/v1/books?keyword=하루키&categoryId=c0...&libraryId=a1b2...&availableOnly=true&page=0&size=20
```

### 4.2 필터의 성격 — 결과 단위와 조건 단위가 다르다

`keyword`와 `libraryId`·`availableOnly`는 성격이 다르다.

| 파라미터 | 평가 대상 | 성격 |
|---|---|---|
| `keyword` | `Book`의 속성 | 결과 단위와 같음 — 단순 비교 |
| `categoryId` | `Book`의 분류 | 결과 단위와 같음, 단 **계층을 먼저 펼쳐야** 비교 가능 (→ C-1) |
| `libraryId` | `BookCopy`의 지점 | **결과 단위와 다름** — 존재 판정 |
| `availableOnly` | `BookCopy`의 상태 | **결과 단위와 다름** — 존재 판정 |

`libraryId`와 `availableOnly`는 "모든 복본이 조건을 만족"이 아니라 **"한 권이라도 만족"** 을 뜻한다. 중앙도서관에 3권 있고 그중 1권만 대출 가능하면, `availableOnly=true`로도 이 책은 결과에 **나온다**.

[01-domain.md 6장](./01-domain.md#6-조회-관점의-구조적-특징)이 지적한 "결과 단위(`Book`)와 조건 단위(`BookCopy`)가 다르다"는 특성이 API 계약에 드러나는 지점이 여기다. 계약상 두 가지를 명시해야 한다.

1. 두 필터는 **존재 판정**이다. 응답에 같은 Book이 복본 수만큼 중복되지 않는다.
2. 따라서 클라이언트는 "이 책이 결과에 있다"에서 "몇 권 빌릴 수 있다"를 유추할 수 없다. 그 수를 별도 필드로 돌려줘야 한다 — [4.3](#43-응답).

### 4.3 응답

`200 OK`

```json
{
  "content": [
    {
      "id": "9e4f...", "isbn": "9788983920683", "title": "노르웨이의 숲",
      "author": "무라카미 하루키", "publisher": "민음사", "publishedAt": "2013-09-01",
      "category": { "id": "c1...", "name": "일본소설" },
      "totalCopies": 3,
      "availableCopies": 1
    }
  ],
  "scope": { "libraryId": "a1b2...", "libraryName": "중앙도서관" },
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
}
```

**집계 스코프는 필터를 따른다.**

| `libraryId` | `totalCopies` / `availableCopies` 기준 | `scope` |
|---|---|---|
| 지정됨 | 그 지점의 복본만 | 해당 지점 정보 |
| 생략됨 | 전 지점 합계 | `null` |

`scope`를 응답에 담는 이유는 같은 `totalCopies: 3`이 "중앙도서관에 3권"일 수도 "전 지점 합쳐 3권"일 수도 있기 때문이다. 클라이언트가 요청 파라미터를 되짚지 않고도 숫자의 의미를 알 수 있어야 한다.

### 4.4 응답에 지점별 내역을 담지 않는 이유

검색 결과에 `copiesByLibrary` 같은 지점별 배열을 **담지 않는다.** 집계 스칼라 두 개만 담는다.

| 근거 | 설명 |
|------|------|
| UC 경계 | 지점별 보유·대출가능 수는 UC-07(도서 상세 조회)의 책임으로 이미 정의되어 있다. 검색 결과에 중첩 배열로 다시 담으면 두 유스케이스의 경계가 흐려진다. |
| 응답 크기 | 목록 화면에서 전 지점 내역은 대부분 쓰이지 않는다. N개 Book × M개 지점의 중첩 배열은 대부분 버려진다. |
| 최소 필요량 | 그러나 `availableOnly` 필터를 쓴 경우 "몇 권 빌릴 수 있는가"가 없으면 필터가 적용됐는지 확인할 수단이 없다. 스칼라 두 개가 그 최소 필요량이다. |

목록에서 책을 고르고 → 상세(UC-07)에서 어느 지점에 가야 하는지 확인하는 흐름을 전제한다. 검색 단계에서 지점을 이미 정한 사용자는 `libraryId` 필터를 걸었을 것이고, 그러면 스코프가 그 지점으로 좁혀져 스칼라 두 개로 충분하다.

### 4.5 에러

| 코드 | HTTP | 규칙 | 발생 조건 |
|------|------|------|-----------|
| `AUTH_TOKEN_INVALID` | 401 | — | 토큰 없음/무효 |
| `LIBRARY_NOT_FOUND` | 404 | — | `libraryId` 대상 없음 |
| `CATEGORY_NOT_FOUND` | 404 | — | `categoryId` 대상 없음 |

검색 조건에 맞는 책이 없는 것은 에러가 아니다. 빈 `content`와 `totalElements: 0`을 반환한다.

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-09-18 | 최초 작성. 엔드포인트 23개(22 UC − UC-13/14 통합 + 보조 2). 에러 코드 규칙 단위 체계, P-8 2층 권한 표기, UC-06 집계 스칼라 방식 확정. |
| 2026-09-18 | 권한 모델을 분기로 변경(1.3절, 에러 표 문구). 지점 정보 수정을 `/contact`·`/name`으로 분할(UC-18/UC-18-1) — 엔드포인트 24개. UC-11의 `USER_NOT_ACTIVE` 규칙 참조를 M-3 → L-13으로 교정. |
| 2026-09-18 | 보조 엔드포인트 2건의 인증 요구 차이에 근거 추가. |
