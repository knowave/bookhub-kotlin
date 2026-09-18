# Deep Interview Spec: docs/adr/0002-duedate-persistence.md 작성

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.

## Metadata
- Interview ID: di-adr0002-20260918
- Rounds: 2 (Round 0 topology + 1 scoring round)
- Final Ambiguity Score: 5.5%
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
| Constraint Clarity | 0.95 | 0.25 | 0.2375 |
| Success Criteria | 0.93 | 0.25 | 0.2325 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9450** |
| **Ambiguity** | | | **0.0550** |

## Topology

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| ADR 0002 | active | `docs/adr/0002-duedate-persistence.md` 신규 — ADR-0001과 동일한 8섹션 | 전체 Acceptance Criteria |

**산출물은 1건뿐이다.** `docs/adr/0001`·`0007`을 포함한 다른 문서와 소스는 읽기만 한다.

---

## Goal

반납 예정일(`dueDate`)을 컬럼으로 저장하기로 한 결정을 ADR-0001과 동일한 8섹션 형식으로 기록한다. 채택하지 않은 대안(계산)을 진지하게 서술하고, **ADR-0007(연체는 계산)과 정반대로 보이는 이유와 그 구분 기준**을 명시한다.

---

## 확정된 내용

### D-1. 형식 — ADR-0001과 동일한 8섹션 (Round 0 확정)

```
# 0002. 반납 예정일 저장
## 상태 / ## 맥락 / ## 검토한 선택지 / ## 결정
## 근거 / ## 포기한 것 / ## 재검토 조건 / ## 관련 문서
```

"함께 다룰 것"(도메인 상수 위치, ADR-0007과의 구분 기준)은 **별도 섹션을 만들지 않고 `## 근거` 안에 녹인다** (Round 0 확정).

### D-2. 검토한 선택지 2개

| # | 선택지 | 서술 방향 |
|---|--------|----------|
| 1 | 저장하지 않고 계산한다 (`loanedAt + 14일`) | **합리적인 경우를 진지하게 쓴다**: 파생 가능한 값을 저장하지 않는 것은 정규화의 기본이다. 컬럼이 하나 줄고 "저장된 값과 계산 결과가 어긋나는" 위험이 원천적으로 없다. **대출 기간이 고정이고 연장 기능이 없다면 계산이 더 낫다** |
| 2 | `dueDate` 컬럼으로 저장한다 | 채택 |

### D-3. 저장을 택한 근거 3가지

| # | 근거 | 핵심 |
|---|------|------|
| 1 | **정책 변경의 소급** | 14일 → 21일로 바꾸면 계산 방식은 진행 중인 과거 대출의 기한까지 함께 바꾼다. 어제 연체였던 기록이 오늘 정상으로 뒤집힌다. `dueDate`는 대출 시점에 맺은 약속이므로 나중에 바뀌면 안 된다 |
| 2 | **연장을 표현할 수 없다** (→ L-4) | `loanedAt + 14 + 연장횟수 × 7`로 풀면 규칙이 계산식에 흩어지고, 연장 기간이 바뀌면 과거 대출까지 영향받는다 |
| 3 | **연체 조회를 DB에서 걸 수 없다** | `where due_date < current_date`가 불가능해져 전체 대출을 애플리케이션으로 가져와 필터링해야 한다 |

### D-4. 근거 안에 녹일 두 가지

**(a) 계산 규칙의 위치**
`LOAN_PERIOD_DAYS = 14` 같은 상수는 **도메인에 두고 결과만 저장한다.** "로직을 도메인에 두는 것"과 "결과를 저장하는 것"은 배타적이지 않다.

**(b) ADR-0007과의 구분 기준 — 이 ADR의 핵심 논지**

> 갈림길은 **"다른 값에서 유도 가능한가"가 아니라 "시간이 지나면 저절로 바뀌는가"** 다.
> `dueDate`는 대출 시점에 확정되는 **약속**이라 고정되고, 연체 여부는 매일 달라지는 **조건**이라 매번 평가해야 한다.
> **고정되는 것은 저장하고, 변하는 것은 계산한다.**

`01-domain.md:185`가 이미 같은 구분을 쓰고 있다 — "`dueDate`는 **약속**, `closedAt`은 **사실**". ADR은 이 구분을 판단 기준으로 승격시킨다.

### D-5. 포기한 것 2가지

| # | 항목 | 내용 |
|---|------|------|
| 1 | **정규화 관점의 중복** | `loanedAt`과 `dueDate`가 함께 저장되므로 둘이 어긋날 수 있다. **어긋나면 `dueDate`가 진실이다** |
| 2 | **정책 변경이 기존 대출에 반영되지 않는다** | 의도된 것이지만, "전체 소급 적용"이 필요한 상황에서는 일괄 갱신 작업이 필요하다 |

### D-6. 재검토 조건 — 3방향 (Round 1 확정)

**되돌리는 방향**
연장 기능이 제거되고 대출 기간이 영구 고정되면 `dueDate`는 파생값으로 돌아간다. 단 **컬럼을 지우는 순간 과거 대출의 기한이 현재 정책으로 소급 변경**되므로 이력 보존을 포기하는 것과 같다. 실질적으로는 "신규 대출부터 적용"이라는 단서가 필요하다.

**강화하는 방향**
- **연장 이력을 개별 추적해야 하면**(누가 언제 몇 번째) `extensionCount` 하나로는 부족해져 `LoanExtension` 같은 테이블이 필요해진다. 그때 `dueDate`는 "현재 유효한 기한"이 되고 이력은 별도로 산다
- **대출 기간이 회원 등급·도서 종류에 따라 분기하면** 저장의 가치가 더 커진다. **"왜 이 대출은 7일이었나"에 답할 근거가 저장된 값뿐**이기 때문이다

**비가역 지점**
`dueDate` 기반 연체 통계나 장서 회전율 리포트가 누적되기 시작하면 되돌리기가 사실상 닫힌다. 과거 리포트와 재계산 결과가 어긋난다.

> **제외된 항목**: 인터뷰에서 "연체 조회 부하 증가"를 재검토 조건 후보로 제시했으나 사용자가 기각했다. 부하가 늘어도 이 결정을 다시 볼 이유가 아니라 **오히려 저장을 지지하는 사실**이므로 (인덱스 조회가 가능한 것이 저장 덕분), 재검토 조건이 아니라 근거 3번을 보강하는 자리에 둔다.

### D-7. 관련 문서

| 대상 | 연결 |
|------|------|
| ADR-0007 | **정반대 방향처럼 보이는 결정.** 구분 기준을 이 ADR이 제시한다 |
| ADR-0001 | 같은 형식. "진실을 한 곳에만 둔다"와 "약속은 고정한다"가 나란히 선다 |
| L-1 | 대출 기간 14일 |
| L-4 | 연장 7일 × 최대 2회 — 근거 2번의 전제 |
| L-9 | 연체는 계산 — 구분 기준의 반대편 사례 |

---

## Constraints

- ADR-0001과 **동일한 8섹션**, 동일한 제목 형식 (`# 0002. …`, 접두사 `ADR-` 없음)
- 한국어, **코드 블록 0개**, 한 페이지 (본문 100줄 이내)
- 규칙은 재서술하지 않고 번호로 참조 (`02-requirements.md`가 SSOT)
- 채택하지 않은 대안을 폄하하지 않는다 — **(1)이 더 나은 경우를 명시적으로 인정**
- 재검토 조건에 `"나중에 필요하면"` 같은 모호한 표현 금지
- 세 문서에 없는 정보는 추측 금지 (인터뷰에서 1건 해소)

## Non-Goals

- `docs/adr/0001-*.md`, `docs/adr/0007-*.md` 수정
- `CONTEXT.md` / `01-domain.md` / `02-requirements.md` / `03-api.md` 수정 (읽기만)
- 소스 코드 변경 (`Loan.kt`는 이미 저장 방식으로 구현되어 있음 — 이 ADR은 사후 기록)
- `LoanExtension` 테이블 설계 (재검토 조건에서 가정적으로만 언급)

---

## Acceptance Criteria

- [ ] 파일 경로가 정확히 `docs/adr/0002-duedate-persistence.md`
- [ ] 제목이 `# 0002. ` 로 시작 (접두사 `ADR-` 없음)
- [ ] 8섹션이 ADR-0001과 **동일한 순서**로 존재
- [ ] `## 상태`가 `채택 (2026-09-18)`
- [ ] `## 검토한 선택지`에 2개, **(1) 계산 방식이 더 나은 경우가 명시적으로 서술됨** (정규화 / 불일치 위험 없음 / 기간 고정+연장 없을 때)
- [ ] `## 근거`에 3가지 근거가 모두 존재 (정책 소급 / 연장 표현 / DB 조회)
- [ ] `## 근거`에 **도메인 상수 위치**("로직은 도메인, 결과는 저장")가 포함
- [ ] `## 근거`에 **ADR-0007과의 구분 기준**("고정되는 것은 저장, 변하는 것은 계산")이 포함
- [ ] `## 포기한 것`에 2건 (정규화 중복 + "어긋나면 dueDate가 진실" / 정책 미소급)
- [ ] `## 재검토 조건`에 **되돌리는 방향 / 강화하는 방향 / 비가역 지점** 3방향 전부
- [ ] 재검토 조건에 "신규 대출부터 적용" 단서 포함
- [ ] 재검토 조건에 **연체 조회 부하가 등장하지 않음** (근거 쪽으로 이동)
- [ ] L-1, L-4, L-9 참조 (전부 실재 검증됨)
- [ ] `## 관련 문서`에 ADR-0001·ADR-0007 링크
- [ ] 코드 블록 0개, 규칙 원문 복사 0건
- [ ] 본문 100줄 이내
- [ ] `docs/adr/0001-*.md`, `docs/adr/0007-*.md` 무변경

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 8섹션 내용이 전부 제시됨 | `재검토 조건`만 비어 있었고, "나중에 필요하면 금지" 제약이 붙어 추측 불가 | Round 1에서 3방향 확정 |
| 이 결정도 되돌릴 수 없을 것 | 0001과 달리 `loanedAt + 14 + extensionCount × 7`로 재계산 가능 | 되돌릴 수 있으나 **정책 변경·리포트 누적 시점부터 닫힌다**는 조건부 가역으로 정리 |
| 연체 조회 부하가 재검토 조건 | 부하가 늘어도 결정을 뒤집을 이유가 아님 | 사용자 기각 — 근거를 지지하는 사실이므로 근거 3번 쪽으로 |
| `LoanExtension`이 도입 대상 | 재검토 조건의 가정적 사례일 뿐 현재 모델에 없음 | 온톨로지에 포함하지 않음. 비고로만 기록 |

---

## Technical Context

- **`Loan.kt`가 이미 저장 방식으로 구현되어 있다.** 이 ADR은 사후 기록이다:
  - `Loan.kt:83-85` — `LOAN_PERIOD_DAYS = 14L`, `EXTENSION_DAYS = 7L`, `MAX_EXTENSION_COUNT = 2` (상수가 도메인에 있음 → D-4(a)의 실제 근거)
  - `Loan.kt:97` — `dueDate = loanedAt.plusDays(LOAN_PERIOD_DAYS)` (생성 시 계산 후 저장)
  - `Loan.kt:78` — `dueDate = dueDate.plusDays(EXTENSION_DAYS)` (연장 시 갱신 → D-3의 근거 2번이 코드로 확인됨)
- `01-domain.md:185` — "`dueDate`는 대출 시점에 정해지고 연장으로 갱신되는 **약속**, `closedAt`은 실제로 일어난 **사실**" (구분 기준의 선행 서술)
- `01-domain.md:43` — "연장은 새 `Loan`을 만들지 않는다. 기존 행의 `dueDate`를 미루고 `extensionCount`를 1 올릴 뿐"
- `01-domain.md:138` — ERD `LOAN` 블록에 `date dueDate "연장 시 갱신된다"`
- 인용 규칙 L-1 / L-4 / L-9 전부 `02-requirements.md`에 실재 확인
- `docs/adr/`에는 `0001`, `0007` 2건 존재. 0002가 세 번째

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Loan | core domain | loanedAt, **dueDate**, status, closedAt?, extensionCount | belongs to User, belongs to BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable), has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| Category | supporting | name, parent? | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

> **비고**: Round 1에서 `LoanExtension`이 언급되었으나, 이는 재검토 조건의 **가정적 미도입 개념**이다. 현재 모델에 존재하지 않으므로 온톨로지 표와 안정성 계산에서 제외했다.

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합 불변. 이 ADR은 새 개념을 만들지 않고 **`Loan.dueDate` 하나의 저장 여부**를 기록한다.

---

## Interview Transcript

<details>
<summary>Full Q&A (2 rounds)</summary>

### Round 0 — Topology
**Q:** 토폴로지(ADR 1건, ADR-0001과 동일한 8섹션)가 맞는가? "함께 다룰 것"을 별도 섹션으로 뺄 것인가?
**A:** ADR-0001과 동일한 8섹션 그대로. (→ 도메인 상수 위치와 ADR-0007 구분 기준은 `## 근거`에 녹임)
**Ambiguity:** 8.8%

### Round 1 — Success Criteria
**Q:** 8섹션 중 `재검토 조건`만 채울 내용이 없다. ADR-0001과 달리 이 결정은 되돌릴 수 있는데(재계산 가능), 어떤 조건으로 쓸 것인가?
**A:** 양방향 + 비가역 지점 3방향.
  - 되돌리기: 연장 제거 + 기간 영구 고정 시. 단 과거 기한이 소급 변경되므로 "신규 대출부터 적용" 단서 필요
  - 강화: 연장 이력 개별 추적(`LoanExtension`) 또는 기간이 등급·종류별 분기. 후자는 "왜 7일이었나"의 유일한 근거가 저장값
  - 비가역: 연체 통계·회전율 리포트 누적 시 과거 리포트와 재계산이 어긋나 닫힘
  - **제시된 "연체 조회 부하" 옵션은 기각** — 재검토 조건이 아니라 이 결정을 지지하는 근거
**Ambiguity:** 5.5%

**인터뷰 중 수행한 검증 (질문 대신):** `Loan.kt`의 상수 3종과 `dueDate` 생성·갱신 지점, `01-domain.md`의 "약속 vs 사실" 서술, 인용 규칙 L-1/L-4/L-9 실재를 모두 확인.

</details>
