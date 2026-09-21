# Deep Interview Spec: 문서 기준 엔티티 정렬

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 수정하지 않았다.
> **⚠️ 이번 세션 처음으로 소스 코드가 대상이다.**

## Metadata
- Interview ID: di-entity-align-20260921
- Rounds: 4 (Round 0 topology + 3 scoring rounds)
- Final Ambiguity Score: 8.0%
- Type: brownfield
- Generated: 2026-09-21
- Threshold: 0.2
- Threshold Source: default
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.95 | 0.35 | 0.3325 |
| Constraint Clarity | 0.93 | 0.25 | 0.2325 |
| Success Criteria | 0.85 | 0.25 | 0.2125 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9200** |
| **Ambiguity** | | | **0.0800** |

## Topology

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| Loan | active | `LoanStatus` 교체, `closedAt` 개명, B-5 연쇄 주도 | D-1 |
| BookCopy | active | 상태 전이 가드 추가, `repair()` 신설 | D-2 |
| User | active | `library` 추가, 팩토리 분리, `promoteToLibrarian()` 제거 | D-3 |
| Category | active | 역방향 컬렉션 제거, `changeParent()`·`changeName()` 신설 | D-4 |

**준수 중이므로 수정하지 않음**: `BaseEntity`(UUID PK·`columnDefinition` 없음 → ADR-0003), `Book`(author 문자열·역방향 없음 → ADR-0004, 0005), `Library`(역방향 없음 → ADR-0005)

---

## Goal

`docs/`의 규칙·불변식·ADR을 기준으로 엔티티 코드를 맞춘다. **엔티티 계층 안에서만 작업하며 새 파일을 만들지 않는다.**

---

## 확정된 변경

### D-1. Loan

| 항목 | 변경 | 근거 |
|------|------|------|
| `LoanStatus` | `OVERDUE` **제거**, `LOST` **추가** → `LOANED` / `RETURNED` / `LOST` | ADR-0007, L-9, L-12 |
| `returnedAt: LocalDate?` | → **`closedAt: LocalDate?`** 개명 | 직전 문서 결정 |
| `returnBook()` | `closedAt`에 실제 반납일 기록 | L-12, UC-12 |
| **`loseCopy()` 신설** | `status == LOANED` 검사 → `bookCopy.markAsLost()` 호출 → 자신을 `LOST`로, `closedAt`에 판정일 | **B-5 연쇄를 Loan이 주도** (Round 2 확정) |
| `isOverdue()` / `overdueDays()` | **유지** | L-9, ADR-0007 |
| `idx_loans_overdue (status, due_date)` | **유지** | ADR-0007이 이 형태가 계산 방식을 커버한다고 명시 |

**연쇄를 Loan이 주도하는 이유**: `Loan`은 `bookCopy`를 정방향으로 참조하므로 둘 다 바꿀 수 있다. 반대 방향은 ADR-0005가 막는다.

### D-2. BookCopy

| 메서드 | 변경 | 근거 |
|--------|------|------|
| `markAsDamaged()` | `check(status == AVAILABLE)` **가드 추가** | B-1, B-4 — 대출 중 파손 금지 |
| `markAsLost()` | `check(status == AVAILABLE \|\| status == LOANED)` **가드 추가** | B-1 — 서가 분실과 대출 중 분실 모두 허용 |
| **`repair()` 신설** | `check(status == DAMAGED)` → `AVAILABLE` | B-2, UC-10-1 |
| `loanOut()` / `returned()` | **유지** (가드 이미 있음) | — |

`markAsLost()`가 `LOANED`도 받는 이유: 서가 분실은 직접 호출되고, 대출 중 분실은 `loan.loseCopy()`가 이 메서드를 호출한다. 두 경로가 같은 메서드로 수렴한다.

### D-3. User (Round 3 확정)

| 항목 | 변경 | 근거 |
|------|------|------|
| **`library: Library?` 추가** | `ManyToOne(LAZY)`, nullable, **단방향** | M-6, P-8, ADR-0005, ADR-0009 |
| **`member(email, password, name)`** | companion 팩토리 — `role=MEMBER`, `status=ACTIVE`, `library=null` | M-1, M-6 |
| **`librarian(email, password, name, library)`** | companion 팩토리 — `role=LIBRARIAN`, `status=PENDING`, **`library` 필수** | M-2, M-6, UC-02 |
| **`promoteToLibrarian()` 제거** | 현재 구현은 역할만 바꿔 **M-6과 M-2를 동시에 깬다.** 대응 UC도 없다 | — |
| `approve()` | **유지** — 이미 `PENDING`일 때만 `ACTIVE`로 전이하고 아니면 예외 | UC-05 |

> **사용자 확정 문구**: "librarian 팩토리가 역할·상태·소속을 함께 세팅한다. 세 값이 항상 같이 정해지므로 흩어질 이유가 없다. 승격 기능이 생기면 그때 소속 지정과 승인 절차를 함께 설계한다."

ADMIN 생성 경로는 두지 않는다 — 초기 SQL로 만든다(M-5).

### D-4. Category

| 항목 | 변경 | 근거 |
|------|------|------|
| **`_children` / `children` 제거** | `@OneToMany(mappedBy="parent")` 역방향 컬렉션 삭제 | **ADR-0005 정면 위반 해소** |
| `parent: Category?` | `val` → **`var`** | UC-20이 상위 변경을 요구하는데 현재 불변 |
| **`changeParent(newParent)` 신설** | 새 상위의 **조상 체인을 `parent`로 거슬러 올라가** 자기 자신을 만나면 거부 | C-2, I-5 |
| **`changeName(name)` 신설** | 현재 `name`이 `var`인데 변경 메서드가 없다 | UC-20 |
| `fullPath()` / `isRoot` | **유지** — `parent`(정방향)만 사용하므로 역방향 제거와 무관 | — |

**C-2 검사가 역방향 없이 성립하는 이유**: 순환은 "새 상위의 조상 중에 내가 있다"와 동치다. 조상 체인은 정방향 `parent`로 탈 수 있으므로 ADR-0005를 지키면서 검사할 수 있다.

---

## Constraints

- **엔티티 계층 안에서만.** 새 파일을 만들지 않는다 — 리포지토리·서비스·DTO 전부 범위 밖
- **ADR-0005 준수**: 역방향 컬렉션을 추가하지 않고, 있던 것(`Category._children`)은 제거한다
- 기존 가드가 있는 메서드(`loanOut`, `returned`, `approve`)는 건드리지 않는다
- 문서에 없는 필드·메서드를 임의로 추가하지 않는다
- `docs/` 전체를 수정하지 않는다 (읽기 전용)

## Non-Goals

- 리포지토리 인터페이스 / 서비스 계층 / 컨트롤러 신설
- 단위 테스트 작성 (Round 2에서 선택지 4번을 택하지 않음)
- `BaseEntity`, `Book`, `Library` 수정 — 이미 문서를 준수
- 데이터 마이그레이션 (`ddl-auto: create-drop`이라 불필요)
- `docs/adr/0003`의 구체적 개수 정리 (별건, 5회 이월 중)

---

## Acceptance Criteria

### Loan
- [ ] `LoanStatus`가 `LOANED` / `RETURNED` / `LOST` — `OVERDUE` 0건
- [ ] `returnedAt` 식별자 0건, `closedAt`으로 개명
- [ ] `returnBook()`이 `closedAt`을 설정
- [ ] `loseCopy()`가 존재하고 `LOANED` 가드 → 복본과 자신을 모두 `LOST`로, `closedAt` 기록
- [ ] `isOverdue()` / `overdueDays()` 유지

### BookCopy
- [ ] `markAsDamaged()`에 `AVAILABLE` 가드
- [ ] `markAsLost()`에 `AVAILABLE || LOANED` 가드
- [ ] `repair()`가 존재하고 `DAMAGED` 가드
- [ ] `loanOut()` / `returned()` 무변경

### User
- [ ] `library: Library?` 필드 (`ManyToOne`, nullable, 역방향 없음)
- [ ] `member(...)` / `librarian(..., library)` 팩토리 존재
- [ ] `librarian(...)`이 `PENDING` + `library` 필수
- [ ] `promoteToLibrarian()` 0건
- [ ] `approve()` 무변경

### Category
- [ ] `@OneToMany` 0건, `_children` / `children` 0건
- [ ] `parent`가 `var`
- [ ] `changeParent()`가 순환을 거부
- [ ] `changeName()` 존재
- [ ] `fullPath()` / `isRoot` 유지

### 전체
- [ ] `./gradlew compileKotlin` 통과
- [ ] `src/main`에 새 파일 0개
- [ ] `docs/` 무변경
- [ ] 엔티티 어디에도 `@OneToMany` 없음 (ADR-0005 전수)

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 격차는 알려진 3~4건 | 전수 대조 결과 `Category` 역방향 컬렉션·`parent` 불변이 추가로 드러남 | 4개 엔티티로 토폴로지 확정 |
| `_children` 제거가 공개 API를 깬다 | 외부 사용처 0건이고 `fullPath()`·`isRoot`는 정방향만 씀 | **`children` 하나만 사라진다** (Round 0 과장 정정) |
| B-5 연쇄를 `BookCopy`가 담는다 | `BookCopy`는 `Loan`을 모른다(ADR-0005) | **`Loan`이 주도** — 정방향 참조를 가진 쪽이 조율 |
| "모든 작업 다" = 서비스 계층까지 | 배타적 선택지를 동시에 할 수 없고, 서비스는 문서에 없는 설계를 요구 | 엔티티 4개만, 연쇄는 Loan 주도 |
| C-2 검사에 역방향이 필요 | 순환 = "새 상위의 조상 중 내가 있다" → 정방향으로 판정 가능 | ADR-0005 지키며 구현 |
| `approve()`를 추가해야 함 | 이미 동일 동작으로 존재 | 유지 |

---

## Technical Context

- `src/main`은 엔티티와 설정만 — 리포지토리·서비스·컨트롤러 없음
- `src/test`에는 컨텍스트 로드 확인 1건뿐 (Kotest + `SpringExtension`)
- 변경 대상 API의 **외부 사용처 0건** — `children`·`fullPath`·`markAsDamaged`·`markAsLost`·`returnedAt`·`OVERDUE` 모두 정의 파일 밖에서 쓰이지 않음
- `application.yaml`: `ddl-auto: create-drop` → 스키마 마이그레이션 불필요
- `BaseEntity`: `@GeneratedValue(strategy = GenerationType.UUID)`, `columnDefinition` 없음 → ADR-0003 준수
- `User`가 `Library`를 참조하게 되면 user → library 도메인 의존이 생긴다. `BookCopy` → `Book`(library → book)이 이미 같은 패턴이므로 새로운 종류의 결합은 아니다

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| User | core domain | email, password, name, role, status, **library?** | belongs to Library (nullable, 단방향) |
| Loan | core domain | loanedAt, dueDate, status, **closedAt?**, extensionCount | belongs to User, belongs to BookCopy (단방향) |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library (단방향) |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category (단방향) |
| Category | supporting | name, **parent (var)** | self-referencing (단방향) |
| Library | supporting | name, address, phone | — |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |
| 2 | 7 | 0 | 0 | 7 | 100% |
| 3 | 7 | 0 | 2 (User·Category 필드 변경) | 5 | 100% |

엔티티 집합 불변. 변화는 전부 기존 엔티티의 필드·메서드 수준이다.

---

## Interview Transcript

<details>
<summary>Full Q&A (4 rounds)</summary>

### Round 0 — Topology
**Q:** 문서 대조 결과 격차가 있는 엔티티는 Loan·BookCopy·User·Category 넷이다. 수정 범위를 어디까지로 잡는가?
**A:** 엔티티 4개 전부.
**Ambiguity:** 22.3%

### Round 1 — Constraint Clarity
**Q:** B-5 연쇄를 어디에 두는가? `BookCopy`는 `Loan`을 참조하지 않는다(ADR-0005).
**A:** "모든 작업 다" — 배타적 선택지를 고르지 않음
**Ambiguity:** 21.6%

### Round 2 — Constraint Clarity (재질문)
**Q:** "모든 작업 다"를 어느 범위로 읽는가? 서비스 계층 신설은 문서에 없는 설계를 요구한다.
**A:** 엔티티 4개만, 연쇄는 Loan 주도.
**Ambiguity:** 11.1%

### Round 3 — Constraint Clarity
**Q:** `User` 생성 경로를 어떻게 맞추는가? `promoteToLibrarian()`은 대응 UC가 없다.
**A:** companion 팩토리 `member` / `librarian` 분리, `librarian`이 역할·상태·소속을 함께 세팅, `promoteToLibrarian()` 제거, `approve()`는 UC-05 대응.
**Ambiguity:** 8.0%

**인터뷰 중 수행한 검증 (질문 대신):** 엔티티 7개 전수 정독, 변경 대상 API의 외부 사용처 0건 확인, 테스트 현황 확인, `fullPath()`/`isRoot`가 정방향만 쓴다는 사실 확인(Round 0 과장 정정), C-2를 정방향으로 검사 가능함을 도출.

</details>
