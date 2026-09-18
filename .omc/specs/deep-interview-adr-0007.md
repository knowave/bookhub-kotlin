# Deep Interview Spec: docs/adr/0007-overdue-as-calculation.md 재작성

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.
> **⚠️ 이 작업은 기존 파일 덮어쓰기다.** 신규 생성이 아니다.

## Metadata
- Interview ID: di-adr0007-20260918
- Rounds: 2 (Round 0 topology + 1 scoring round)
- Final Ambiguity Score: 6.5%
- Type: brownfield
- Generated: 2026-09-18
- Threshold: 0.2
- Threshold Source: default
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.95 | 0.35 | 0.3325 |
| Constraint Clarity | 0.92 | 0.25 | 0.2300 |
| Success Criteria | 0.92 | 0.25 | 0.2300 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9350** |
| **Ambiguity** | | | **0.0650** |

## Topology

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| ADR 0007 | active | `docs/adr/0007-overdue-as-calculation.md` **덮어쓰기** — 0001·0002와 동일한 8섹션 | 전체 Acceptance Criteria |

**산출물은 1건.** 기존 파일(4,067 bytes, 6섹션, 11:06 작성)을 전면 교체한다. 다른 문서와 소스는 읽기만 한다.

> **선행 결정 번복 기록**: ADR-0001 인터뷰 Round 0에서 `"0001만 작성, 0007은 그대로"`를 선택해 형식 불일치를 의도적으로 남겼다. 이번 작업이 그 결정을 뒤집어 ADR 세트를 8섹션으로 통일한다.

---

## Goal

`LoanStatus`에 `OVERDUE`를 두지 않고 연체를 계산으로만 판정한 결정을, ADR-0001·0002와 동일한 8섹션 형식으로 재작성한다. 선택지 3개를 진지하게 비교하고, ADR-0002가 이 결정의 **전제**임을 밝힌다.

---

## 확정된 내용

### D-1. 형식 — 0001·0002와 동일한 8섹션

```
# 0007. 연체는 상태가 아니라 계산으로 판정한다
## 상태 / ## 맥락 / ## 검토한 선택지 / ## 결정
## 근거 / ## 포기한 것 / ## 재검토 조건 / ## 관련 문서
```

제목에 접두사 `ADR-` 없음. 기존 헤더 메타(`- 상태: 수락됨 / - 날짜: / - 관련:`)는 `## 상태`와 `## 관련 문서`로 흡수한다.

### D-2. 검토한 선택지 3개

| # | 선택지 | 서술 방향 |
|---|--------|----------|
| 1 | `OVERDUE` 상태를 저장하고 배치로 전환한다 | **합리적인 경우를 진지하게 쓴다**: 상태가 컬럼에 있으면 조회가 단순해지고(`where status = 'OVERDUE'`), 연체 전환 시점에 알림 발송 같은 후속 작업을 붙이기 쉽다. 상태 기계가 하나로 통일되어 읽기도 편하다 |
| 2 | 계산으로만 판정한다 | 채택 |
| 3 | 저장과 계산을 병행한다 | **이 프로젝트가 실제로 처했던 상태.** `LoanStatus`에 `OVERDUE`가 있는데 아무도 저장하지 않고, `isOverdue()`는 `status == LOANED`일 때만 동작하는 모순. 두 진실이 공존하면 코드마다 어느 쪽을 믿을지 판단이 갈린다 |

(3)은 가상의 대안이 아니라 **실제 초기 상태**였다는 점을 분명히 쓴다. 이 ADR은 그 모순을 해소한 기록이다.

### D-3. 결정문 (Round 1 확정 — 사용자 제시 원문)

> 연체를 상태로 저장하지 않고 계산으로 판정한다.
>
> `LoanStatus`는 `LOANED` / `RETURNED` / `LOST` 세 값만 갖는다.
> 연체 여부는 `status == LOANED && dueDate < 오늘`로 매번 평가하며, 연체 전환 배치는 만들지 않는다.
>
> 응답에는 `status`와 별도로 `isOverdue`, `overdueDays`를 계산 필드로 담는다.

**기존 0007의 "`LOANED`, `RETURNED` 두 값만 남는다"는 사실과 달라진 서술이다.** 이후 L-12·B-5 결정으로 `LOST`가 추가되었으므로, 재작성 시 **3값으로 교정**한다.

### D-4. 근거 3가지

| # | 근거 | 핵심 |
|---|------|------|
| 1 | **배치 시점과 실제 시점의 불일치** | 자정 배치라면 오전에 기한이 지난 건이 14시간 동안 연체가 아닌 것으로 조회된다. 배치가 실패하면 하루 종일 틀린다 |
| 2 | **진실이 둘이 된다** | `status`를 믿을지 `dueDate` 비교를 믿을지 정해야 하고, 그 판단이 코드 곳곳에 흩어진다 |
| 3 | **상태와 조건의 구분** | `LOANED`·`RETURNED`·`LOST`는 누군가의 행위로 전이되며 화살표마다 원인이 되는 유스케이스가 있다. 연체는 아무 행위 없이 시간이 지나면 성립한다. **화살표를 그릴 원인이 없는 것은 상태가 아니라 조건이다** |

### D-5. ADR-0002와의 관계 — 근거 안에 포함

**이 결정이 성립하는 전제가 ADR-0002다.** `dueDate`가 컬럼으로 저장되어 있기 때문에 `where status = 'LOANED' and due_date < current_date`가 인덱스를 타고 돌아간다. 저장하지 않았다면 계산 방식은 전체 조회 후 필터링이 되었을 것이다.

> **중요 제약**: ADR-0002가 정리한 기준(`"고정되는 것은 저장하고 변하는 것은 계산한다"`)을 **이 문서에서 반복 서술하지 않는다. 참조만 한다.** 두 ADR이 같은 문장을 두 번 쓰면 어느 쪽이 원본인지 흐려진다.

### D-6. 포기한 것 3가지

| # | 항목 |
|---|------|
| 1 | **연체 전환 시점을 후킹할 지점이 없다.** 알림 발송이나 제재 부과처럼 "연체가 된 순간"에 무언가를 해야 하는 기능은 별도 설계가 필요하다 |
| 2 | **조회할 때마다 계산이 일어난다.** 목록 응답에서 건마다 날짜 비교가 붙는다 (실질 부담은 작지만 공짜는 아니다) |
| 3 | **응답 DTO가 `status` 외에 `isOverdue`, `overdueDays`를 별도로 가져야 한다.** 클라이언트는 상태 하나로 분기할 수 없고 두 값을 함께 봐야 한다 |

### D-7. 재검토 조건

연체료 부과, 연체 알림 발송, 장기 연체자 제재가 생기면 다시 본다.

**다만 그때도 `LoanStatus`에 `OVERDUE`를 넣는 것이 아니라 별도 테이블(연체 이력, 알림 발송 기록)로 간다.** 그것들은 대출의 상태가 아니라 독립된 사건이기 때문이다.

### D-8. 미완 작업은 ADR에 기록하지 않는다 (Round 1 확정)

> ADR은 **결정의 시점 기록**이며 작업 추적기가 아니다. 미완 작업은 별도 이슈나 TODO로 관리한다.

따라서:
- 기존 `## 후속 작업` 섹션(체크박스 2건)을 **되살리지 않는다**
- 현재 코드 상태(`Loan.kt:106`에 `OVERDUE` 잔존, `LOST` 미추가)를 **ADR 본문에 쓰지 않는다**
- 8섹션 외에 9번째 섹션을 만들지 않는다

---

## Constraints

- ADR-0001·0002와 **동일한 8섹션**, 동일한 제목 형식 (`# 0007. …`, 접두사 없음)
- 한국어, **코드 블록 0개**, 한 페이지 (본문 100줄 이내)
- 규칙은 재서술하지 않고 번호로 참조
- 채택하지 않은 선택지를 폄하하지 않는다 — **(1)이 주는 이점 3가지를 명시적으로 인정**
- **ADR-0002의 판단 기준 문장을 복사하지 않는다** — 참조만
- 미완 작업·현재 코드 상태를 본문에 쓰지 않는다
- 세 문서와 ADR-0002에 없는 정보는 추측 금지

## Non-Goals

- `docs/adr/0001-*.md`, `docs/adr/0002-*.md` 수정
- `CONTEXT.md` / `01-domain.md` / `02-requirements.md` / `03-api.md` 수정 (읽기만)
- 소스 코드 변경 (`Loan.kt`의 `OVERDUE` 제거 등은 이 작업 범위 밖)
- 기존 `## 후속 작업` 체크박스 보존
- 기존 `대안 B`("반납 처리 시점에만 연체 여부 기록") 보존 — 사용자가 지정한 선택지 3개에 없음

---

## Acceptance Criteria

- [ ] `docs/adr/0007-overdue-as-calculation.md`를 **덮어쓴다** (신규 경로 생성 아님)
- [ ] 제목이 `# 0007. ` 로 시작, 접두사 `ADR-` 없음
- [ ] 8섹션이 ADR-0001·0002와 **동일한 순서**
- [ ] `## 상태`가 `채택 (2026-09-18)` (기존 "수락됨" 표기 교체)
- [ ] 선택지 3개, **(1)의 이점 3가지가 명시적으로 서술됨** (조회 단순 / 후속 작업 후킹 / 상태 기계 통일)
- [ ] (3)이 **실제 초기 상태였음**이 명시됨
- [ ] `## 결정`이 D-3 원문대로 — **`LOANED`/`RETURNED`/`LOST` 3값** (기존 "두 값만"은 교정)
- [ ] `## 근거`에 3가지 근거 전부
- [ ] `## 근거`에 ADR-0002가 **전제**임이 서술됨 (인덱스 조회 가능한 이유)
- [ ] **ADR-0002의 "고정되는 것은 저장하고 변하는 것은 계산한다" 문장이 복사되지 않음** — 참조 링크만
- [ ] `## 포기한 것` 3건
- [ ] `## 재검토 조건`에 3가지 트리거 + **"OVERDUE가 아니라 별도 테이블로 간다"** 방향
- [ ] `## 관련 문서`에 ADR-0001·0002 링크 + 규칙 번호
- [ ] **`## 후속 작업` 섹션 없음**, 9번째 섹션 없음
- [ ] 본문에 현재 코드 상태(`Loan.kt` 등) 언급 없음
- [ ] 코드 블록 0개, 규칙 원문 복사 0건, 본문 100줄 이내
- [ ] `docs/adr/0001-*.md`, `0002-*.md` 무변경

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 신규 파일 작성 | 0007이 이미 존재(11:06, 6섹션) | 덮어쓰기임을 확인. ADR-0001 Round 0 결정을 뒤집는 것도 명시 |
| 기존 내용을 보존해야 함 | `후속 작업`·`대안 B`가 새 형식에 자리 없음 | Round 1: **ADR은 결정 기록이지 작업 추적기가 아님** → 후속 작업 제외. 대안 B는 지정 선택지 3개에 없어 제외 |
| 기존 결정문을 그대로 옮기면 됨 | "LOANED, RETURNED 두 값만"이 L-12(LOST 추가) 이후 사실과 다름 | 3값으로 교정. 결정문 원문 확정 |
| 현재 코드 상태를 ADR에 적어야 함 | 코드는 아직 OVERDUE를 갖고 LOST가 없음 | 기록하지 않음. ADR은 결정 시점 기록 |

---

## Technical Context

- **기존 `docs/adr/0007-overdue-as-calculation.md`** (11:06, 4,067 bytes, 6섹션): 배경 / 결정 / 결정 근거 / 고려했으나 선택하지 않은 대안(A·B) / 결과(긍정·부정) / 후속 작업
- 기존 문서의 `## 배경`이 서술한 초기 모순이 새 문서의 선택지 (3)과 `## 맥락`의 재료다:
  - `isOverdue()`/`overdueDays()`는 계산 — `status == LOANED && 기준일 > dueDate`
  - `LoanStatus.OVERDUE`는 선언만 되고 아무 코드도 할당하지 않음 (도달 불가능한 값)
  - 인덱스 `idx_loans_overdue (status, due_date)`는 배치 갱신 구조를 전제한 형태
- 인용 규칙 실재 확인: **L-3**(연체 시 신규 대출 차단), **L-9**(연체는 계산), **L-12**(대출 종료 사유 2종, `LOST` 포함)
- `CONTEXT.md:29` — `status: LOANED | RETURNED | LOST`
- `CONTEXT.md:43` — "LoanStatus에 OVERDUE를 두지 않는다. 연체 전환 배치도 만들지 않는다"
- `docs/adr/` 현황: `0001`(15:49), `0002`(16:30), `0007`(11:06). 0002가 8섹션 기준의 최신 사례
- **참고(본문에 쓰지 않음)**: `Loan.kt:106`에 `OVERDUE`가 잔존하고 `LOST`가 없다. D-8에 따라 ADR에 기록하지 않는다

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Loan | core domain | loanedAt, dueDate, **status**, closedAt?, extensionCount | belongs to User, belongs to BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable), has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| Category | supporting | name, parent? | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

> **비고**: 재검토 조건에서 "연체 이력"·"알림 발송 기록" 테이블이 언급되었으나, 조건부 미도입 개념이므로 온톨로지와 안정성 계산에서 제외했다.

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합 불변. 이 ADR은 **`LoanStatus` enum의 값 구성** 하나를 기록한다.

---

## Interview Transcript

<details>
<summary>Full Q&A (2 rounds)</summary>

### Round 0 — Topology
**Q:** 0007이 이미 존재한다(6섹션). 덮어쓰면 `후속 작업` 체크박스(미완)와 `대안 B`가 사라지는데 어떻게 할 것인가? (ADR-0001 Round 0의 "0007은 그대로" 결정을 뒤집는 것이기도 함)
**A:** (최초) 둘 다 살려서 흡수 → **(수정) 무조건 0007에 덮어써**
**Ambiguity:** 11.1%

### Round 1 — Constraint Clarity
**Q:** 기존 결정문 "LOANED, RETURNED 두 값만 남는다"가 L-12(LOST 추가) 이후 사실과 어긋난다. 코드도 아직 OVERDUE를 갖고 LOST가 없다. 새 `## 결정`을 어떻게 쓰고 미완 작업은 어디에 둘 것인가?
**A:** 결정문 원문 제시 (3값 명시). **"현재 코드 상태는 ADR에 기록하지 않는다. ADR은 결정의 시점 기록이며 작업 추적기가 아니다. 미완 작업은 별도 이슈나 TODO로 관리한다."**
**Ambiguity:** 6.5%

**인터뷰 중 수행한 검증 (질문 대신):** 기존 0007 전문, `CONTEXT.md`의 `LoanStatus` 3값 정의, L-3/L-9/L-12 실재, `Loan.kt:106`의 `OVERDUE` 잔존, 0001·0002의 8섹션 목록을 모두 확인.

</details>
