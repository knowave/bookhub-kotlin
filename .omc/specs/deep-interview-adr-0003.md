# Deep Interview Spec: docs/adr/0003-uuid-primary-key.md 작성 + 기존 2건 정리

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.

## Metadata
- Interview ID: di-adr0003-20260918
- Rounds: 2 (Round 0 topology + 1 scoring round)
- Final Ambiguity Score: 7.0%
- Type: brownfield
- Generated: 2026-09-18
- Threshold: 0.2
- Threshold Source: default
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.93 | 0.35 | 0.3255 |
| Constraint Clarity | 0.93 | 0.25 | 0.2325 |
| Success Criteria | 0.92 | 0.25 | 0.2300 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9305** |
| **Ambiguity** | | | **0.0695** |

## Topology

Round 0에서 산출물이 1건 → **3건으로 확장**되었다.

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| ADR 0003 | active | `docs/adr/0003-uuid-primary-key.md` **신규** — 8섹션 | D-1 ~ D-7 |
| ADR 0002 정리 | active | `docs/adr/0002-duedate-persistence.md:75` 순서 관계 표현 제거 | D-8 |
| ADR 0007 정리 | active | `docs/adr/0007-overdue-as-calculation.md:80` 순서 관계 표현 제거 | D-8 |

---

## Goal

모든 엔티티의 기본키를 UUID로 한 결정을 기존 ADR과 동일한 8섹션으로 기록한다. 동시에 `관련 문서` 섹션의 기준을 **"내용상 실제로 얽히는 것만"** 으로 통일하고, 기존 2건에 남은 순서 관계 표현을 제거한다.

---

## 확정된 내용

### D-1. 형식

0001·0002·0007과 동일한 8섹션. 제목 `# 0003. ` (접두사 `ADR-` 없음), 상태 `채택 (2026-09-18)`.

### D-2. 검토한 선택지 2개

| # | 선택지 | 서술 방향 |
|---|--------|----------|
| 1 | `Long` + IDENTITY (auto-increment) | **합리적인 경우를 진지하게 쓴다**: 순차 정수는 인덱스에 순서대로 삽입되어 B-tree 페이지 분할이 거의 없다. 8바이트로 UUID의 절반이며 모든 외래키에도 그 차이가 곱해진다. **사람이 읽고 말할 수 있어** 로그 추적이나 운영 대응에서 편하다. 단일 데이터베이스에서 순차 발급이 문제되지 않는 규모라면 **이쪽이 더 나은 선택** |
| 2 | UUID | 채택 |

### D-3. UUID를 택한 근거 3가지

| # | 근거 | 확인된 뒷받침 |
|---|------|--------------|
| 1 | **식별자 노출.** 순차 ID는 URL이나 응답에 실릴 때 총 회원 수나 대출 건수를 추측할 수 있게 한다 | `03-api.md:162-174` — `loans/{id}`·`book-copies/{id}`가 실제로 경로에 노출됨 |
| 2 | **애플리케이션에서 미리 생성 가능하다.** DB 왕복 없이 식별자를 정할 수 있어 연관 객체를 한 번에 조립할 수 있다 | — |
| 3 | **병합과 이관에서 충돌이 없다.** 지점별 데이터를 합치거나 환경 간에 옮길 때 키 재매핑이 필요 없다 | — |

### D-4. 포기한 것 4가지

| # | 항목 | 확인된 뒷받침 |
|---|------|--------------|
| 1 | **인덱스 삽입 성능.** 랜덤 UUID는 B-tree에 무작위 위치로 삽입되어 페이지 분할이 잦다. 대출 이력이 계속 누적되는 `loans`에서 체감될 수 있다 | — |
| 2 | **저장 공간.** 8 → 16바이트이며 모든 FK 컬럼과 인덱스에 반복된다. `loans`는 FK 2개 | `01-domain.md:142-143` — `user_id`, `book_copy_id` 확인 |
| 3 | **가독성.** 로그 추적이나 문의 대응에서 사람이 읽고 받아적기 어렵다 | — |
| 4 | **정렬.** 순차 ID는 생성 순서를 담지만 UUID는 담지 않는다. 생성 순 정렬은 `createdAt`에 의존해야 한다 | `CONTEXT.md:12` — `BaseEntity`에 `createdAt` 존재 |

### D-5. 근거 안에 녹일 것 — DB 타입 매핑

**Hibernate가 처리하며 `columnDefinition`을 명시하지 않는다.** PostgreSQL은 `uuid` 타입을 네이티브로 지원하고, MySQL의 `BINARY(16)` 같은 벤더 종속 정의를 엔티티에 박으면 이식성이 사라진다.

별도 섹션을 만들지 않고 `## 근거` 안에 소제목으로 녹인다 (0002·0007과 같은 방식).

### D-6. 재검토 조건 3방향 (Round 1 확정)

**되돌리는 방향 — 측정 근거를 전제로 한다.**
추측이 아니라 **측정된 병목**이 있을 때만 다시 본다. PK 타입 변경은 6개 엔티티의 PK와 7개 FK 컬럼을 전부 재매핑해야 하므로, "느릴 것 같다"로는 착수할 수 없다.

**강화하는 방향 — UUIDv7.**
삽입 성능이나 정렬이 실제 문제가 되면 랜덤 UUID를 **시간 정렬 가능한 UUIDv7로 바꾼다.** 포기한 것 4건 중 2건(인덱스 삽입, 정렬)을 **PK 타입을 유지한 채** 해소한다. 되돌리기보다 훨씬 싼 경로다.

**비가역 지점 (사용자 제시).**
외부 시스템이나 클라이언트가 이 식별자를 저장하기 시작하면 변경이 닫힌다.

### D-7. 관련 문서 — "내용상 실제로 얽히는 것만"

**규칙 번호는 넣지 않는다.** `L`/`M`/`P`/`C`/`B` 37개 중 UUID 기본키와 내용상 얽히는 규칙이 없음을 확인했다. 없는 참조를 채워 넣지 않는다.

| 대상 | 내용 관계 |
|------|----------|
| `CONTEXT.md` | `BaseEntity`가 모든 엔티티에 `id: UUID`를 상속시키는 지점 |
| `01-domain.md` | ERD가 PK 6개·FK 7개를 UUID로 표기 — 포기한 것 2번(저장 공간)의 근거 |
| `03-api.md` | 식별자가 경로에 노출되는 지점 — 근거 1번의 근거 |
| ADR-0001 | **판단 필요 항목** (아래 참조) |

> **ADR-0001 포함 여부는 작성자 판단**: Book/BookCopy 분리로 복본 행이 늘어나면 UUID 16바이트 비용이 그만큼 곱해진다. 이는 순서 관계가 아니라 **실제 내용 관계**이므로 포함하되, 그 관계를 한 줄로 명시한다. 사용자가 불필요하다고 보면 제거한다.

### D-8. 기존 2건 정리 (Round 0 확정)

| 파일 | 현재 | 조치 |
|------|------|------|
| `0002-duedate-persistence.md:75` | `\| [ADR-0001] \| 같은 형식의 첫 결정. "진실을 한 곳에만 둔다"와 "약속은 고정한다"가 나란히 선다 \|` | **순서 관계("같은 형식의 첫 결정")만 제거**하고 내용 관계는 유지 |
| `0007-overdue-as-calculation.md:80` | `\| [ADR-0001] \| 같은 형식의 첫 결정 \|` | 내용 관계가 없으므로 **행 전체 제거** |

두 파일의 다른 내용은 건드리지 않는다.

---

## Constraints

- 0001·0002·0007과 **동일한 8섹션**, 제목 `# 0003. ` (접두사 없음)
- 한국어, **코드 블록 0개**, 한 페이지 (본문 100줄 이내)
- **`관련 문서`에는 내용상 실제로 얽히는 것만.** "같은 형식의 첫 결정" 같은 순서 관계 금지
- **현재 코드 상태를 기록하지 않는다.** ADR은 결정의 시점 기록이다
- (1)을 폄하하지 않는다 — **"이쪽이 더 나은 선택"인 경우를 명시**
- DB 타입 매핑은 별도 섹션 없이 `## 근거` 안에
- 규칙 원문 복사 금지

## Non-Goals

- `docs/adr/0001-book-bookcopy-separation.md` 수정
- `CONTEXT.md` / `01-domain.md` / `02-requirements.md` / `03-api.md` 수정 (읽기만)
- 소스 코드 변경
- `0002`·`0007`의 `관련 문서` 외 다른 부분 수정

---

## Acceptance Criteria

### docs/adr/0003-uuid-primary-key.md (신규)
- [ ] 제목이 `# 0003. ` 로 시작, 접두사 `ADR-` 없음
- [ ] 8섹션이 0001·0002·0007과 **동일한 순서**
- [ ] `## 상태`가 `채택 (2026-09-18)`
- [ ] 선택지 2개, **(1)의 이점 4종이 명시적으로 서술됨** (페이지 분할 적음 / 8바이트 / FK에 차이 곱해짐 / 사람이 읽을 수 있음)
- [ ] **"이쪽이 더 나은 선택"류 인정 문장 존재**
- [ ] 근거 3가지 전부 (식별자 노출 / 미리 생성 / 병합·이관)
- [ ] `## 근거`에 DB 타입 매핑 소제목 — `columnDefinition` 미명시, PostgreSQL 네이티브, `BINARY(16)` 이식성
- [ ] `## 포기한 것` 4건 (삽입 성능 / 저장 공간 / 가독성 / 정렬)
- [ ] `## 재검토 조건`에 3방향 — 되돌리기(측정 근거 전제) / 강화(UUIDv7) / 비가역(외부 저장)
- [ ] 재검토 조건에 **UUIDv7이 포기한 것 2건을 PK 타입 유지한 채 해소**한다는 점 명시
- [ ] `## 관련 문서`에 **순서 관계 표현 0건**, 각 항목에 내용 관계가 한 줄로 적힘
- [ ] `## 관련 문서`에 **규칙 번호 없음** (얽히는 규칙이 없으므로)
- [ ] 본문에 현재 코드 상태(`Loan.kt` 등 파일명·구현 현황) 언급 없음
- [ ] 코드 블록 0개, 본문 100줄 이내

### 기존 2건 정리
- [ ] `0002:75`에서 `"같은 형식의 첫 결정"` 제거, 내용 관계 문구는 유지
- [ ] `0007:80` ADR-0001 행 제거
- [ ] 두 파일의 다른 부분 무변경 (8섹션 구조·다른 행 유지)
- [ ] 세 문서 전체에서 `"같은 형식"` 문자열 0건

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 산출물은 ADR 1건 | 지적하신 순서 관계 표현이 기존 0002·0007에 실재 | Round 0: 기존 2건도 함께 정리 (3건) |
| 재검토 조건을 추측할 수 있음 | "나중에 필요하면" 금지 제약 + 비가역 지점만 제시됨 | Round 1: 강화=UUIDv7, 되돌리기=측정 근거 전제 |
| 되돌리기와 강화가 대칭 | PK 재매핑(PK 6 + FK 7)은 비가역 지점 이전에도 매우 비쌈. UUIDv7은 PK 타입 유지 | 비대칭임을 명시 — 강화가 훨씬 싼 경로 |
| 관련 문서에 규칙 번호가 들어감 | 37개 규칙 중 UUID PK와 얽히는 것이 없음 | 규칙 번호 없이 문서 참조만. 없는 참조를 만들지 않음 |

---

## Technical Context

- `CONTEXT.md:12` — "모든 엔티티는 `BaseEntity` 상속 (`id: UUID`, `createdAt`/`updatedAt`: `Instant`)"
- `01-domain.md` ERD — `uuid id PK` 6개(USER, LIBRARY, BOOK, CATEGORY, BOOK_COPY, LOAN), `uuid … FK` 7개(`library_id`×2, `category_id`, `parent_id`, `book_id`, `user_id`, `book_copy_id`)
- `03-api.md:162-174` — `book-copies/{id}/damage-report`·`repair`·`lost-report`, `loans/{id}/return`·`extensions` 등 식별자가 경로에 직접 노출
- `docs/adr/` 현황: `0001`(8섹션), `0002`(8섹션), `0007`(8섹션) — 세 파일 모두 동일 구조
- 정리 대상 실제 문자열 확인:
  - `0002:75` → `| [ADR-0001](./0001-book-bookcopy-separation.md) | 같은 형식의 첫 결정. "진실을 한 곳에만 둔다"와 "약속은 고정한다"가 나란히 선다 |`
  - `0007:80` → `| [ADR-0001](./0001-book-bookcopy-separation.md) | 같은 형식의 첫 결정 |`
- 기술 스택: Kotlin 2.3 / Spring Boot 4.1 / JPA·Hibernate / PostgreSQL (`uuid` 네이티브 지원)

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| BaseEntity | supporting | **id(UUID)**, createdAt, updatedAt | 전 엔티티 상속 — 이 ADR의 직접 대상 |
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable), has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| Loan | core domain | loanedAt, dueDate, status, closedAt?, extensionCount | belongs to User, belongs to BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| Category | supporting | name, parent? | self-referencing, has many Book |

`BaseEntity`가 이 ADR의 중심이다. 다른 6개는 그 결정을 상속받는 쪽이다.

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합 불변. 이 ADR은 **모든 엔티티가 공유하는 식별자 타입** 하나를 기록한다.

---

## Interview Transcript

<details>
<summary>Full Q&A (2 rounds)</summary>

### Round 0 — Topology
**Q:** 토폴로지(ADR 1건, 8섹션)가 맞는가? 지적하신 "같은 형식의 첫 결정" 표현이 기존 `0002:75`·`0007:80`에 실재하는데 같이 지울 것인가?
**A:** 0003 작성 + 기존 2건도 정리.
**Ambiguity:** 11.3%

### Round 1 — Success Criteria
**Q:** `재검토 조건`의 되돌리기/강화 방향을 무엇으로 채울 것인가? (비가역 지점은 제시됨. 되돌리기는 PK 6 + FK 7 재매핑이라 이미 매우 비쌈)
**A:** 강화는 UUIDv7, 되돌리기는 측정 근거 전제.
**Ambiguity:** 7.0%

**인터뷰 중 수행한 검증 (질문 대신):** 주신 근거 3건(경로 노출, `loans` FK 2개, `createdAt` 존재)이 문서에 실재함을 확인. 규칙 37개 중 UUID PK와 얽히는 것이 없음을 확인. `0002:75`·`0007:80`의 정리 대상 문자열을 원문으로 확보.

</details>
