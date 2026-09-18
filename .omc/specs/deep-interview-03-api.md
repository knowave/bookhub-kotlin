# Deep Interview Spec: docs/03-api.md 작성

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.

## Metadata
- Interview ID: di-03api-20260918
- Rounds: 4 (Round 0 topology + 3 scoring rounds)
- Final Ambiguity Score: 6.6%
- Type: brownfield
- Generated: 2026-09-18
- Threshold: 0.2
- Threshold Source: default
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.94 | 0.35 | 0.3290 |
| Constraint Clarity | 0.94 | 0.25 | 0.2350 |
| Success Criteria | 0.91 | 0.25 | 0.2275 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9340** |
| **Ambiguity** | | | **0.0660** |

## Topology

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| 1. 공통 규약 | active | JWT 클레임 / 에러 형식 / 페이징 / 날짜 / 권한 표기법 | D-3, D-4, D-5, D-6 |
| 2. 엔드포인트 목록 | active | 표(메서드·경로·권한·소속제약·설명·대응UC), 도메인별 그룹 | D-1, D-2 — 총 23행 |
| 3. 엔드포인트 상세 | active | 요청·응답·에러 표 + JSON 예시 각 1쌍 | D-4 에러 표 형식 |
| 4. 도서 검색 API 상세 | active | UC-06 전용 절 | D-7 |

**산출물은 `docs/03-api.md` 1건뿐이다.** 다른 문서 수정이나 소스 변경은 범위 밖.

---

## Goal

`docs/CONTEXT.md` · `docs/01-domain.md` · `docs/02-requirements.md`를 입력으로 `docs/03-api.md`를 작성한다.
도메인 규칙은 `02-requirements.md`가 단일 출처이며, 재서술하지 않고 번호로 참조한다.
엔드포인트는 유스케이스와 확정된 매핑 규칙(D-1)에 따라 대응시키고, 각 행에 대응 UC를 표기한다.

---

## 확정된 결정 (인터뷰 산출)

### D-1. UC ↔ 엔드포인트 매핑 규칙

기본은 UC 하나에 엔드포인트 하나. 다음 경우만 예외로 한다.

| # | 예외 | 처리 |
|---|------|------|
| 1 | **여러 UC → 한 엔드포인트** | 행위와 상태 전이가 동일하고 권한 검사만 다를 때. 호출자 역할은 토큰으로 판별 가능하므로 URL을 나누지 않는다. |
| 2 | **한 UC → 여러 엔드포인트** | 이 문서에는 해당 사례가 없다. 발생하면 UC 정의가 너무 넓다는 신호이므로 `02-requirements.md`의 UC 분할을 먼저 검토한다. |
| 3 | **엔드포인트만 있고 UC 없음** | 다른 UC의 사전조건을 충족시키기 위한 조회. 표에 **"보조"** 로 표기하고 어떤 UC를 위한 것인지 명시한다. |

각 엔드포인트에 대응 UC를 표기하고, **1:1이 아닌 경우 그 이유를 한 줄로 덧붙인다.**

### D-2. 엔드포인트 구성 (UC 22개 → 엔드포인트 23개)

**예외 1 적용 — 통합 1건**

| 통합 대상 | 엔드포인트 | 사유 |
|---|---|---|
| UC-13(본인 연장) + UC-14(대행 연장) | `POST /api/v1/loans/{id}/extensions` | 상태 전이 동일(dueDate 갱신), 검증 조건 동일(L-4, L-5). 차이는 소유권 검증(L-8) 유무이고 이는 토큰 role로 판별 가능. |

**복본 상태 3건 — 분리 유지** (진입 조건이 B-1·B-4로 서로 다름)

| UC | 엔드포인트 |
|---|---|
| UC-10 복본 파손 처리 | `POST /api/v1/book-copies/{id}/damage-report` |
| UC-10-1 복본 수선 완료 처리 | `POST /api/v1/book-copies/{id}/repair` |
| UC-10-2 복본 분실 처리 | `POST /api/v1/book-copies/{id}/lost-report` |

**예외 3 적용 — 보조 엔드포인트 2건**

| 엔드포인트 | 어떤 UC를 위한 것인가 |
|---|---|
| `GET /api/v1/libraries` | UC-02 사서 가입 시 선택할 지점 목록. 인증 불필요(가입 전 호출). M-7이 기본 지점을 보장한다. |
| `GET /api/v1/categories` | UC-08 도서 등록 시 선택할 카테고리, UC-06 검색 필터용. 계층 구조를 담아 반환한다. |

합계: 22 UC − 1(통합) + 2(보조) = **23 엔드포인트**

### D-3. 공통 규약 — 인증 / base path / 날짜

| 항목 | 결정 |
|------|------|
| base path | **`/api/v1`** (경로에 직접 포함. `context-path` 설정으로 주입하지 않는다) |
| 인증 | JWT Bearer. 클레임: `userId`, `role`, `libraryId` |
| `libraryId` 클레임 | MEMBER·ADMIN은 `null` (→ M-6). LIBRARIAN만 값을 가진다 |
| 인증 불필요 | UC-01, UC-02, UC-03, `GET /libraries` |
| `LocalDate` | `yyyy-MM-dd` |
| `Instant` | ISO-8601 |

### D-4. 에러 응답 설계

**구조**
- `common`에 `BusinessException` 추상 클래스 — `code`, `status`, `message`를 갖는다
- 도메인별 패키지에 구체 예외 클래스를 정의한다
- 에러 코드는 **`{도메인}_{사유}` 형식으로 규칙 단위**로 부여한다. 도메인 단위로만 묶지 않는다 — 프론트가 메시지를 파싱해 분기하게 되므로
- 응답에 `rule` 필드로 위반 규칙 번호(`L-2`, `B-4` 등)를 포함한다. **운영 환경에서는 제외하고 로그에만 남긴다**

**HTTP 상태 코드 기준**

| 상태 | 용도 |
|------|------|
| 400 | 요청 형식 오류 (검증 실패) |
| 401 | 인증 실패 |
| 403 | 권한 부족 (역할 불일치, 소속 지점 불일치) |
| 404 | 대상 없음 |
| 409 | 도메인 규칙 위반, 상태 전이 위반, 중복 |

**도메인 규칙 위반에 400을 쓰지 않는다.** 요청 자체는 올바르고 현재 상태가 허용하지 않는 것이므로 409가 맞다.

**3장 각 엔드포인트마다 에러 표**: `코드 | HTTP | 규칙 | 발생 조건`

### D-5. 페이징 (리포지토리 근거로 도출 — 질문하지 않음)

Spring Boot 4.1 + Spring Data JPA 스택(`build.gradle.kts`, `application.yaml` 확인)이므로 `Pageable` 규약을 따른다.

| 파라미터 | 기본값 |
|---|---|
| `page` | 0 (0-based) |
| `size` | 20 |
| `sort` | 엔드포인트별 기본 정렬 명시 |

적용 대상: UC-04(승인 대기 목록), UC-06(도서 검색), UC-15(본인 대출 이력), UC-16(지점 대출 현황)

### D-6. 권한 표기법 (P-8 2층 검사를 표에서 구분)

목록 표에 **권한**과 **소속 제약**을 별도 열로 둔다.

| 표기 | 의미 |
|------|------|
| 권한 열 | 필요한 최소 역할 (MEMBER / LIBRARIAN / ADMIN / 없음) |
| 소속 제약 열 `—` | 역할 검사만으로 충분 |
| 소속 제약 열에 조건 명시 | P-8 2층 검사 대상. 예: `복본의 지점 == 토큰 libraryId` |

이 두 열 조합이 `02-requirements.md`의 액터별 행위 표와 1:1로 대응되어야 한다.

### D-7. 도서 검색 API (§4) — 판단이 필요한 항목

**응답에 지점별 보유/대출가능 수를 포함할 것인가 → 포함하지 않는다. 집계 스칼라 2개만 담는다.**

| 필드 | 의미 |
|---|---|
| `totalCopies` | 필터 스코프 내 복본 수 |
| `availableCopies` | 필터 스코프 내 `AVAILABLE` 복본 수 |

판단 근거:
- `02-requirements.md` UC-07이 이미 "지점별 복본 보유 수 / 대출 가능 수"를 담당한다. 검색 결과에 중첩 배열로 다시 담으면 두 UC의 경계가 흐려진다.
- 목록 화면에서 전 지점 내역은 대부분 쓰이지 않는다. N개 Book × M개 지점의 중첩 배열은 대부분 버려진다.
- 그러나 `availableOnly` 필터를 쓴 경우 "몇 권 빌릴 수 있는가"가 없으면 필터가 적용됐는지 확인할 수단이 없다. 스칼라 2개가 최소 필요량이다.
- **스코프는 필터를 따른다**: `libraryId` 필터가 있으면 그 지점 기준, 없으면 전 지점 합계. 응답에 스코프를 명시한다.

**"결과 단위와 조건 단위가 다르다"가 API 계약에 드러나는 방식** (→ `01-domain.md` 6장):
- 결과는 `Book` 단위인데 필터(`libraryId`, `availableOnly`)는 `BookCopy`에 걸린다
- 따라서 이들은 **존재 판정 필터**다 — "모든 복본이 조건을 만족"이 아니라 "한 권이라도 만족"
- `totalCopies`/`availableCopies`는 그 존재 판정이 몇 건에 매칭됐는지를 되돌려주는 스칼라다. 조건 단위(BookCopy)의 정보를 결과 단위(Book)로 접어 넣는 지점이 API 계약에서 바로 여기다

**파라미터 생략 시 기본 동작**

| 파라미터 | 생략 시 |
|---|---|
| `keyword` | 전체 조회 (제목·저자·ISBN 대상) |
| `libraryId` | 전 지점 |
| `categoryId` | 전 분류. 지정 시 **하위 카테고리 포함** (→ C-1) |
| `availableOnly` | `false` — 대출 불가 도서도 결과에 포함 |

---

## Constraints

- **SSOT**: 규칙은 `02-requirements.md` 번호로 참조, 재서술 금지
- 요청 body에 `userId`를 넣지 않는다. 토큰에서 추출한다. **단 사서가 대출/반납/연장 대행을 처리할 때는 대상 회원 ID를 body에 받는다**
- 응답에 `password`를 절대 노출하지 않는다
- 연체 정보는 저장 상태가 아니므로(→ L-9) 응답에 `isOverdue`, `overdueDays`를 **계산 필드**로 담는다. `status`에는 `LOANED`/`RETURNED`/`LOST`만 나간다
- **`DELETE` 엔드포인트를 만들지 않는다** (1차 범위 제외)
- JSON 예시는 엔드포인트당 요청/응답 **하나씩만**
- 대출 종료일 필드명은 `closedAt` (직전 작업에서 `returnedAt`에서 개명됨)

## Non-Goals

- `docs/01-domain.md`, `docs/02-requirements.md`, `docs/CONTEXT.md` 수정
- 소스 코드 작성 (`BusinessException` 클래스 등은 설계만 서술, 구현 안 함)
- OpenAPI/Swagger 스펙 파일 생성
- 인증 토큰 발급·갱신 상세 구현 (재발급·만료 정책은 1차 범위 밖)

---

## Acceptance Criteria

### 공통
- [ ] 모든 경로가 `/api/v1` 접두사를 가진다
- [ ] Kotlin 코드 블록 없음 (JSON 예시는 허용)
- [ ] 도메인 규칙을 재서술하지 않고 번호로 참조

### §1 공통 규약
- [ ] JWT 클레임 3종(`userId`, `role`, `libraryId`) + `libraryId` nullable 사유
- [ ] 에러 응답 구조 + `rule` 필드 + 운영 환경 제외 방침
- [ ] HTTP 상태 코드 5종 기준표 + "규칙 위반에 400을 쓰지 않는" 근거
- [ ] 페이징 `page`/`size`/`sort` 규약
- [ ] 날짜 형식 2종
- [ ] 권한 표기법: 권한 열 + 소속 제약 열 구분 정의 (→ P-8)

### §2 엔드포인트 목록
- [ ] 23행 (22 UC − 1 통합 + 2 보조)
- [ ] 열: 메서드 / 경로 / 권한 / 소속 제약 / 설명 / 대응 UC
- [ ] 도메인별 그룹핑 (인증·회원 / 도서 / 복본 / 대출 / 운영)
- [ ] UC-13+UC-14 통합 행에 통합 사유 한 줄
- [ ] 보조 2건에 "보조" 표기 + 어떤 UC를 위한 것인지
- [ ] `DELETE` 0건
- [ ] 소속 제약 열이 `02-requirements.md` 액터별 행위 표와 일치

### §3 엔드포인트 상세
- [ ] 엔드포인트마다 요청(path/query/body + 제약) / 응답(상태코드 + body) / 에러 표
- [ ] 에러 표 열: `코드 | HTTP | 규칙 | 발생 조건`
- [ ] UC-11(대출 처리) 에러에 L-2, L-3, L-10, P-8, M-3 전부 포함
- [ ] UC-10(파손) 에러에 B-4 포함
- [ ] UC-13/14(연장) 에러에 L-4, L-5, L-8 포함
- [ ] 대출 응답에 `isOverdue`, `overdueDays` 계산 필드
- [ ] 회원 응답에 `password` 없음
- [ ] 대행 처리(대출/반납/연장) body에만 대상 회원 ID
- [ ] JSON 예시 엔드포인트당 정확히 요청 1 + 응답 1

### §4 도서 검색
- [ ] 파라미터 4종 조합 방식 + 생략 시 기본 동작 표
- [ ] 카테고리 하위 포함 동작 (→ C-1)
- [ ] `totalCopies`/`availableCopies` 포함 결정 + 판단 근거
- [ ] 필터 스코프에 따른 집계 기준 명시
- [ ] "결과 단위(Book) ≠ 조건 단위(BookCopy)"가 계약에 드러나는 방식 서술 (→ 01-domain 6장)

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| UC가 20개이고 엔드포인트와 1:1 | UC-10 분할로 실제 22개 | 매핑 규칙 D-1 수립 (예외 3종) |
| 모든 UC가 독립 엔드포인트 | UC-13/14는 상태 전이가 동일하고 권한만 다름 | 통합 1건, 토큰 role로 분기 |
| 복본 상태 변경은 하나의 PATCH | 진입 조건이 B-1·B-4로 서로 다름 | 3개 엔드포인트 유지 |
| 엔드포인트는 전부 UC에 대응 | 지점·카테고리 목록은 UC 사전조건용인데 UC가 없음 | "보조" 분류 신설 (예외 3) |
| 규칙 위반은 400 | 요청 형식은 올바르고 상태가 허용하지 않는 것 | 409로 통일, 400은 검증 실패 전용 |
| 에러 코드는 도메인 단위로 묶어도 됨 | 프론트가 메시지를 파싱해 분기하게 됨 | 규칙 단위 `{도메인}_{사유}` |
| 페이징 방식을 물어야 함 | Spring Data JPA 스택이 규약을 이미 결정 | `Pageable` 도출, 질문하지 않음 |
| base path는 추론 가능 | `application.yaml`에 `context-path` 없음, `@RestController` 0건 | 질문 → `/api/v1` |

---

## Technical Context

- Kotlin 2.3 / Spring Boot 4.1 / JPA / PostgreSQL / Kotest / JWT
- `application.yaml`: `open-in-view: false`, `threads.virtual.enabled: true`, `ddl-auto: create-drop`, H2 콘솔 활성
- **컨트롤러·서비스·DTO 계층 전무** — `@RestController` 0건. 이 문서가 그 계층의 첫 설계 입력이 된다
- 엔티티 7종 존재하나 문서와 어긋난 부분이 있다 (미승인 대기 항목):
  - `User.library` 필드 없음 → `libraryId` 클레임의 원천이 아직 코드에 없다
  - `LoanStatus`에 `LOST` 없고 `OVERDUE`가 남아 있음
  - `Loan.returnedAt` → `closedAt` 미개명
  - `BookCopy.markAsDamaged()`/`markAsLost()` 가드 없음, `repair()` 없음
  - → **문서는 목표 상태를 기술하고, 코드는 아직 따라오지 않았다.** 03-api.md는 목표 상태 기준으로 쓴다

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable), has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| Loan | core domain | loanedAt, dueDate, status, closedAt?, extensionCount | belongs to User, belongs to BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| Category | supporting | name, parent? | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |
| 2 | 7 | 0 | 0 | 7 | 100% |
| 3 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합 불변. 이번 인터뷰의 결정은 전부 **엔티티가 아니라 계약 표면**에 관한 것이었다.

---

## Interview Transcript

<details>
<summary>Full Q&A (4 rounds)</summary>

### Round 0 — Topology
**Q:** 4개 섹션 토폴로지가 맞는가?
**A:** 4개 그대로 진행.
**Ambiguity:** 18.5%

### Round 1 — Goal Clarity (엔드포인트 목록)
**Q:** "UC-01~20 1:1"이 실제 22개 UC와 불일치. UC-13/14(같은 행위·다른 액터)와 UC-10 계열 3건을 어떻게 가르는가?
**A:** 매핑 규칙 수립 — 기본 1:1, 예외 3종(다수 UC→1 엔드포인트 / 1 UC→다수는 UC 분할 신호 / UC 없는 보조 엔드포인트). 연장은 `POST /loans/{id}/extensions`로 통합, 복본은 damage-report / repair / lost-report 3개 유지.
**Ambiguity:** 13.1%

### Round 2 — Success Criteria (엔드포인트 상세)
**Q:** 도메인 규칙 위반을 에러 응답으로 어떻게 내보내는가? (UC-11 하나에 L-2·L-3·L-10·P-8·M-3가 겹침)
**A:** `BusinessException` 추상 클래스 + 도메인별 구체 예외. 코드는 `{도메인}_{사유}` 규칙 단위. `rule` 필드로 규칙 번호 포함(운영 제외). HTTP 400/401/403/404/409 기준 확정, 규칙 위반은 409.
**Ambiguity:** 8.8%

### Round 3 — Context Clarity (공통 규약)
**Q:** API base path는? (`application.yaml`에 `context-path` 없음, `@RestController` 0건 확인 후 질문)
**A:** `/api/v1` — 버전 포함.
**Ambiguity:** 6.6%

</details>
