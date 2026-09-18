# Deep Interview Spec: docs/01-domain.md 작성 + 복본/대출 상태 전이 확장

> **상태: PENDING APPROVAL** — 이 spec은 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.

## Metadata
- Interview ID: di-01domain-20260918
- Rounds: 4 (Round 0 topology + 3 scoring rounds)
- Final Ambiguity Score: 5.8%
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
| Success Criteria | 0.92 | 0.25 | 0.2300 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9425** |
| **Ambiguity** | | | **0.0575** |

## Topology

Round 0에서 `docs/01-domain.md` 6개 섹션으로 확정했으나, Round 1~2 답변으로 **산출물이 1개 → 4개로 확장**되었다.

### A. docs/01-domain.md (신규) — Round 0 확정 6섹션

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| 1. 도메인 용어 사전 | active | 개념 정의 표 | Book/BookCopy 구분, "복사본 아님", Loan=사건·연장=dueDate 갱신 |
| 2. 도메인 경계 | active | user/book/library/loan 책임 | Category→book, BookCopy→library, Loan 독립 근거 |
| 3. 엔티티 관계 | active | Mermaid `erDiagram` + 관계 설명 | PK/FK, User→Library(nullable), Category 자기참조, nullable 3건 사유 |
| 4. 상태 전이 | active | Mermaid `stateDiagram-v2` ×3 | BookCopy 4상태, Loan **3상태**, User 2상태 + 전이별 UC 표기 |
| 5. 주요 불변식 | active | 불변식 + 위반 시 영향 | 예시 5건 + 신규 전이로 파생된 항목 |
| 6. 조회 관점 구조적 특징 | active | 왜 까다로운지만 서술 | 예시 3건 |

### B. 파생 산출물 (Round 1~2 답변에서 확장)

| Component | Status | Description |
|-----------|--------|-------------|
| docs/02-requirements.md | active | UC-10 3분할, B-1~B-5 신설, L-12 추가, 액터별 행위 표 갱신 |
| docs/CONTEXT.md | active | `Loan.status` 3값으로 갱신, 복본 전이 규칙 반영 |
| BookCopy.kt / Loan.kt | active | 상태 전이 메서드 + `LoanStatus.LOST` |

### 명시적 제외
| 항목 | 사유 | 확정 |
|------|------|------|
| `docs/adr/0007-overdue-as-calculation.md` | Round 3에서 "CONTEXT만 갱신, ADR은 그대로" 선택 | 2026-09-18 |

---

## Goal

`docs/CONTEXT.md`와 `docs/02-requirements.md`를 단일 출처로 삼아 `docs/01-domain.md`를 작성한다.
이 문서는 "무엇을 해야 하는가"가 아니라 **"개념이 어떻게 구성되어 있는가"**를 다룬다.
동시에, 인터뷰에서 확정된 복본·대출 상태 전이 규칙을 소스 문서와 엔티티에 반영해
01-domain.md의 상태 전이 섹션이 실제 설계와 일치하도록 한다.

---

## 확정된 도메인 결정 (인터뷰 산출)

### D-1. BookCopy 상태 전이 (6개, 이외 금지)

| From | To | 원인 |
|------|-----|------|
| AVAILABLE | LOANED | 대출 (UC-11) |
| LOANED | AVAILABLE | 반납 (UC-12) |
| LOANED | LOST | 대출 중 분실 판정 (UC-10-2) |
| AVAILABLE | DAMAGED | 파손 판정 (UC-10) |
| AVAILABLE | LOST | 서가 분실 판정 (UC-10-2) |
| DAMAGED | AVAILABLE | 수선 완료 (UC-10-1) |

- `LOANED → DAMAGED`는 **금지**. 파손된 책은 실물이 돌아오므로 반납이 선행 가능하다.
- `LOST`는 **최종 상태**.

### D-2. Loan 상태 전이 (LoanStatus 3값으로 확장)

| From | To | 원인 |
|------|-----|------|
| LOANED | RETURNED | 반납 (UC-12) |
| LOANED | LOST | 복본 분실 판정 (UC-10-2, B-5 연쇄) |

`LOANED / RETURNED / LOST` 세 값. **`OVERDUE`는 여전히 없다** (연체 = 계산, ADR-0007 유효).

### D-3. UC 재편 (번호 체계는 위임받아 결정)

기존 UC-10 하나를 셋으로 나눈다. **UC-11 이후는 재부여하지 않고 `-1`, `-2` 접미 방식을 쓴다.**
(P-8 적용 목록, 추가 유스케이스 표 등 UC 번호를 참조하는 곳이 여럿이라, 밀어쓰기는 수정 범위를 불필요하게 넓힌다.)

| ID | 유스케이스 | 사전조건 |
|----|-----------|---------|
| UC-10 | 복본 파손 처리 | 복본 `AVAILABLE`, 복본의 지점 == 사서 소속 |
| UC-10-1 | 복본 수선 완료 처리 | 복본 `DAMAGED`, 복본의 지점 == 사서 소속 |
| UC-10-2 | 복본 분실 처리 | 복본 `AVAILABLE` 또는 `LOANED`, 복본의 지점 == 사서 소속 |

### D-4. 신설 규칙

**§4.5 복본 (신규 절)**

| # | 규칙 | 근거 |
|---|------|------|
| B-1 | 복본 상태 전이는 D-1의 6가지만 허용한다. | 정의되지 않은 전이를 허용하면 Loan과 복본 상태가 어긋난다. |
| B-2 | 파손된 복본은 수선 후 대출 가능 상태로 복귀할 수 있다. | 파손은 물리적으로 회복 가능한 손상이며, 수선 비용을 들인 장서를 영구 폐기하는 것은 낭비다. |
| B-3 | 분실(LOST)은 최종 상태이며 다른 상태로 전이하지 않는다. | 분실된 실물은 시스템이 확인할 수 없다. 발견되는 경우가 있으나 되돌리기 유스케이스가 필요해 1차 범위에서 제외한다. |
| B-4 | 대출 중인 복본은 파손 처리할 수 없다. 먼저 반납해야 한다. 단 분실은 대출 중에도 처리할 수 있다. | 파손된 책은 실물이 돌아오므로 반납이 선행 가능하지만, 분실된 책은 반납 자체가 불가능하다. |
| B-5 | 대출 중인 복본을 분실 처리하면 해당 대출도 LOST로 종료된다. | 복본만 LOST로 바꾸면 대출이 LOANED로 남아 회원의 대출 한도를 영구히 점유하고, 기한이 지나면 연체로 신규 대출까지 막힌다. |

**§4.1 대출에 추가**

| # | 규칙 | 근거 |
|---|------|------|
| L-12 | 대출은 반납(RETURNED) 또는 복본 분실(LOST)로 종료된다. | 분실은 반납과 다른 종료 사유이며, 변상 처리나 통계에서 구분되어야 한다. 연체와 달리 사서의 명시적 판정으로 전이되므로 계산이 아닌 상태로 저장한다. (→ L-9와 대비) |

---

## Constraints

- **SSOT**: 도메인 규칙은 `02-requirements.md`가 단일 출처. 01-domain.md는 규칙을 재서술하지 않고 번호(L-1, P-8, B-3…)로 참조한다.
- **코드 블록 금지**: 필드명 언급은 가능하나 Kotlin 코드 블록을 쓰지 않는다.
- **추측 금지**: 두 소스 문서에 없는 정보는 질문한다. (이 인터뷰에서 3건 해소)
- **Mermaid 정확도**: GitHub에서 렌더링되어야 하므로 `erDiagram` / `stateDiagram-v2` 문법을 정확히 쓴다.
- ERD는 전 컬럼이 아니라 관계 이해에 필요한 것 위주.
- 한국어 작성, 표·목록 중심 (CONTEXT.md 작성 규칙).

## Non-Goals

- `docs/adr/0007-overdue-as-calculation.md` 수정 — Round 3에서 명시적 제외
- 구현 방법 서술 (섹션 6은 "왜 까다로운지"만, 해결책 금지)
- 분실 도서 변상·회수 처리 (B-3에 따라 1차 범위 밖)
- `LOST` → 다른 상태 되돌리기 유스케이스

---

## Acceptance Criteria

### docs/01-domain.md
- [ ] 6개 섹션이 지시된 순서로 존재
- [ ] 용어 사전에 "BookCopy는 원본의 복사본이 아님 / Book 1개 ↔ BookCopy 5개" 명시
- [ ] 용어 사전에 "연장은 새 행이 아니라 기존 행의 dueDate 갱신" 명시
- [ ] 도메인 경계에 Category→book, BookCopy→library, Loan 독립 근거 3건 모두 포함
- [ ] `erDiagram`에 PK/FK 표기, `User → Library`(nullable), `Category` 자기참조 포함
- [ ] nullable 3건(`User.library`, `Category.parent`, `Loan.returnedAt`)의 사유 설명
- [ ] `stateDiagram-v2` 3개, 전이마다 원인 UC 표기
- [ ] BookCopy 다이어그램이 D-1의 6개 전이와 정확히 일치 (`LOANED → DAMAGED` 없음)
- [ ] Loan 다이어그램이 `LOANED / RETURNED / LOST` 3상태, `OVERDUE` 없음
- [ ] Loan 다이어그램 아래에 "연체가 왜 상태가 아닌지" 설명 (L-9 참조)
- [ ] 불변식 표에 예시 5건 + 각 항목 위반 시 영향 1줄
- [ ] 조회 특징 3건, 구현 방법 미언급
- [ ] Kotlin 코드 블록 0개
- [ ] 규칙 재서술 없이 번호 참조

### docs/02-requirements.md
- [ ] UC-10 → UC-10 / UC-10-1 / UC-10-2 분할
- [ ] §4.5 복본 절 신설, B-1 ~ B-5
- [ ] §4.1에 L-12 추가
- [ ] 액터별 행위 표에 "복본 수선 완료 처리", "복본 분실 처리" 반영 (소속 제약: 복본의 지점 == 소속)
- [ ] P-8 적용 대상 목록에 UC-10-1, UC-10-2 반영
- [ ] 변경 이력 1행 추가

### docs/CONTEXT.md
- [ ] `Loan.status`를 `LOANED | RETURNED | LOST`로 갱신 (연체=계산 주석 유지)
- [ ] `[대출]` 또는 신규 항목에 복본 전이 규칙 반영
- [ ] ADR-0007 링크·문구는 **변경하지 않음**

### 엔티티
- [ ] `BookCopy`: `markAsDamaged`(AVAILABLE만), `markAsLost`(**AVAILABLE 또는 LOANED**), `repair`(DAMAGED만)
- [ ] `LoanStatus`에 `LOST` 추가
- [ ] 대출 중 복본 분실 시 Loan도 LOST로 종료 (B-5 연쇄)

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| DAMAGED/LOST는 종착 상태일 것 | 수선 완료·분실도서 발견 경로가 문서에 없음 | DAMAGED는 수선으로 복귀, LOST는 최종 (B-2, B-3) |
| 파손·분실은 같은 유스케이스 | 대출 중 처리 가능 여부가 서로 다름 | UC-10(파손) / UC-10-2(분실)로 분리, B-4 |
| 대출 중 분실은 "반납 후 LOST" | 실물 없는 책을 반납 처리하면 Loan이 RETURNED로 남아 거짓 기록 | `LOANED → LOST` 직접 전이 허용, Loan도 LOST로 종료 (B-5) |
| LoanStatus는 2값 (ADR-0007) | 분실이 반납과 구분되어야 함 | `LOST` 추가해 3값. 연체는 여전히 계산 (L-9 유효) |
| ADR-0007도 함께 고쳐야 함 | ADR은 당시 결정의 기록 | CONTEXT만 갱신, ADR-0007 미수정 (Round 3) |
| UC 번호를 밀어서 재부여 | P-8·추가 UC 표 등 참조처가 여럿 | `-1`, `-2` 접미 방식으로 수정 범위 최소화 (D-3) |

---

## Technical Context

- Kotlin 2.3 / Spring Boot 4.1 / JPA / PostgreSQL / Kotest / JWT
- 엔티티 7종 구현 중 (미커밋): BaseEntity, User, Book, Category, Library, BookCopy, Loan
- `Loan.kt`: `isOverdue()` / `overdueDays()` 계산 로직 존재, `extend()`·`returnBook()`이 `status == LOANED` 검사
- `Loan.kt` 기존 후속 작업(ADR-0007): `LoanStatus`에서 `OVERDUE` 제거 — **아직 미적용**. 이번 `LOST` 추가와 같은 파일을 건드린다.
- `BookCopy.kt` 현황 (확인됨): `loanOut()`·`returned()`는 가드 있음. **`markAsDamaged()`·`markAsLost()`는 이미 존재하나 `check` 가드가 없어 어떤 상태에서도 호출된다.** `repair()`는 없음. → B-1 전이 제약이 코드에 전혀 걸려 있지 않은 상태.
- `User.kt` 현황 (확인됨): **`library` 필드가 없다.** 사서 소속은 문서(M-6, P-8)에만 정의되어 있고 엔티티에 미반영. 이번 작업의 선행 조건은 아니지만, 불변식 "role이 LIBRARIAN인 User는 library가 null이 아니다"를 코드로 검증할 수 없는 상태다.
- ⚠️ **Round 1에서 제시된 `markAsLost` 코드 스니펫은 무효**: `check(status == AVAILABLE)`로 되어 있으나 Round 2 결정(`LOANED → LOST` 허용)에 따라 `AVAILABLE` 또는 `LOANED`를 받아야 한다.

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable, LIBRARIAN만), has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| Loan | core domain | loanedAt, dueDate, status, returnedAt?, extensionCount | belongs to User, belongs to BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| Category | supporting | name, parent? | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 1 (BookCopy 전이 확장) | 6 | 100% |
| 2 | 7 | 0 | 1 (Loan 상태 3값) | 6 | 100% |
| 3 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합은 인터뷰 내내 불변. 변화는 전부 기존 엔티티의 상태 기계 내부에서 일어났다.

---

## Interview Transcript

<details>
<summary>Full Q&A (4 rounds)</summary>

### Round 0 — Topology
**Q:** 6개 섹션 토폴로지가 맞는가? 추가/제거/병합할 것이 있는가?
**A:** 6개 그대로 진행.
**Ambiguity:** 12.5%

### Round 1 — Constraint Clarity (상태 전이)
**Q:** BookCopy의 DAMAGED/LOST에서 나가는 전이가 있는가? (UC-10은 진입만 정의, §5는 "삭제 안 함"만 명시)
**A:** DAMAGED → AVAILABLE(수선) 허용, LOST 최종. LOANED → DAMAGED 금지 (반납 선행). UC-10 분할 + B-1~B-4 + BookCopy 메서드 3개 지시.
**Ambiguity:** 8.5%

### Round 2 — Constraint Clarity (대출 중 분실)
**Q:** B-4대로면 대출 중 분실 시 실물 없는 책을 반납 처리해야 하고 Loan이 RETURNED로 남는다. 어떻게 정리하는가?
**A:** `LOANED → LOST` 직접 전이 허용. `AVAILABLE → LOST`(서가 분실)도 유지. `LOANED → DAMAGED`는 계속 금지. UC-10-2 신설, B-1~B-5 재정의, `LoanStatus.LOST` + L-12 추가.
**Ambiguity:** 7.0%

### Round 3 — Constraint Clarity (ADR 파급)
**Q:** `LoanStatus.LOST` 추가가 ADR-0007 본문("LOANED, RETURNED 두 값만")과 어긋난다. 어떻게 처리하는가?
**A:** CONTEXT만 갱신, ADR은 그대로.
**Ambiguity:** 5.8%

</details>
