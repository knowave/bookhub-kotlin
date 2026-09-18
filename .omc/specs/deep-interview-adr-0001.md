# Deep Interview Spec: docs/adr/0001-book-bookcopy-separation.md 작성

> **상태: PENDING APPROVAL** — 요구사항 기록이며, 아직 어떤 파일도 생성/수정하지 않았다.

## Metadata
- Interview ID: di-adr0001-20260918
- Rounds: 2 (Round 0 topology + 1 scoring round)
- Final Ambiguity Score: 6.3%
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
| Success Criteria | 0.90 | 0.25 | 0.2250 |
| Context Clarity | 0.95 | 0.15 | 0.1425 |
| **Total Clarity** | | | **0.9375** |
| **Ambiguity** | | | **0.0625** |

## Topology

| Component | Status | Description | Coverage |
|-----------|--------|-------------|----------|
| ADR 0001 | active | `docs/adr/0001-book-bookcopy-separation.md` 신규 — 8섹션 템플릿 | 전체 Acceptance Criteria |

**산출물은 1건뿐이다.** 다른 문서 수정은 범위 밖. 특히 `docs/adr/0007-overdue-as-calculation.md`는 **형식이 다르지만 그대로 둔다**(Round 0 확정).

---

## Goal

`docs/CONTEXT.md` · `docs/01-domain.md` · `docs/02-requirements.md`를 근거로, Book과 BookCopy를 분리한 결정을 ADR로 기록한다. 채택하지 않은 대안(수량 컬럼)을 **진지하게** 서술해 결정의 가치가 드러나게 한다.

---

## 확정된 내용 (사용자 제시 + 인터뷰 확정)

### D-1. 문서 형식 — 8섹션 (Round 0 확정)

```
# 0001. Book과 BookCopy 분리
## 상태          채택 (2026-09-18)
## 맥락          어떤 문제 상황에서 필요했는지 / 결정하지 않으면 무엇이 막히는지
## 검토한 선택지  최소 2개. 채택하지 않은 쪽도 합리적인 경우를 함께
## 결정          한두 문장
## 근거          02-requirements.md 규칙 번호 참조
## 포기한 것      비용·제약·복잡도
## 재검토 조건    구체적 조건 ("나중에 필요하면" 금지)
## 관련 문서      다른 ADR과 규칙 번호
```

**기존 ADR-0007과 형식이 다르다** (0007: 배경/결정/결정 근거/고려했으나 선택하지 않은 대안/결과/후속 작업). Round 0에서 **0007을 건드리지 않고 0001만 새 형식으로 쓰기로 확정**했다. ADR 세트 내 형식 불일치는 알고 남기는 상태다.

### D-2. 검토한 선택지 2개

| # | 선택지 | 서술 방향 |
|---|--------|----------|
| 1 | `Book` 단일 엔티티 + 수량 컬럼 (`totalCount`, `availableCount`) | **합리적인 경우를 반드시 함께 쓴다**: 지점이 하나뿐이고 복본별 상태 구분이 불필요하면 훨씬 단순하다. 조인도 서브쿼리도 없고 "몇 권 남았나"가 컬럼 하나로 끝난다 |
| 2 | `Book`과 `BookCopy` 분리 | 채택 |

### D-3. 분리 근거 (규칙 참조 — 전수 검증 완료)

복본마다 개별 관리가 필요한 속성: **청구기호**(지점 내 위치 식별), **소속 지점**, **상태**(`AVAILABLE`/`LOANED`/`DAMAGED`/`LOST`)

| 근거 | 참조 | 검증 |
|------|------|------|
| 대출은 언제나 특정 한 권을 대상으로 한다 | I-1, I-2 | 01-domain.md 불변식에 실재 |
| 복본 상태 전이 규칙이 BookCopy를 전제 | B-1 ~ B-5 | 02-requirements.md §4.5에 실재 |
| 사서 소속 지점 제약이 BookCopy를 전제 | P-8 | 02-requirements.md §4.3에 실재 |

핵심 논거: **수량 컬럼으로는 "3권 중 강남점 1권은 파손, 2권은 대출 중"을 표현할 수 없다.** 권 단위 식별이 없으면 반납 시 어떤 실물이 돌아왔는지, 그 실물이 파손되었는지 판정할 수 없다.

> **예시 지점명은 `강남점`을 그대로 쓴다** (Round 1 확정). 다른 문서의 예시(중앙도서관/동부분관/서부분관)와 다르지만, ADR은 독립적으로 읽히는 문서이므로 사용자가 제시한 표현을 유지한다.

### D-4. 포기한 것 4건

| # | 항목 | 참조 |
|---|------|------|
| 1 | 조회 복잡도 — 결과 단위(Book)와 조건 단위(BookCopy)가 달라져 "대출 가능한 복본이 있는 Book" 검색이 존재 판정을 요구 | 01-domain.md 6장, 03-api.md §4.2 |
| 2 | 단순 조인 시 같은 Book이 복본 수만큼 중복 반환 → 집계 필요 | — |
| 3 | 저장 비용 — 같은 책 5권이면 행 5개 | — |
| 4 | 등록 절차 2단계 — 서지정보 등록과 복본 등록이 분리되어 "책은 등록됐는데 실물이 없는 상태"가 정상으로 존재 | UC-08, UC-09 |

### D-5. 재검토 조건

- **되돌리기 어렵다**: 복본 단위 대출 이력이 쌓인 뒤 수량 컬럼으로 되돌리면 과거 이력의 대상을 특정할 수 없다
- **반대 방향 확장은 열려 있다**: 복본에 구입일·구입가·장정 형태 같은 개별 속성이 더 필요해지면 BookCopy에 추가하면 된다

---

## Constraints

- 한국어
- **한 페이지 이내** — 길어지면 결정이 여러 개 섞인 것
- **코드를 넣지 않는다**
- 규칙은 재서술하지 않고 번호로 참조 (02-requirements.md가 SSOT)
- 세 문서에 없는 정보는 추측 금지 (인터뷰에서 1건 해소)
- 채택하지 않은 대안을 "명백히 나쁘다"로 쓰지 않는다

## Non-Goals

- `docs/adr/0007-overdue-as-calculation.md` 수정 또는 형식 정렬
- `docs/adr/TEMPLATE.md` 생성
- ADR 0002~0006 작성
- `CONTEXT.md` / `01-domain.md` / `02-requirements.md` / `03-api.md` 수정 (읽기만)
- 소스 코드 변경

---

## Acceptance Criteria

- [ ] 파일 경로가 정확히 `docs/adr/0001-book-bookcopy-separation.md`
- [ ] 제목이 `# 0001. Book과 BookCopy 분리` (접두사 `ADR-` 없음 — 지시 형식 그대로)
- [ ] 8개 섹션이 지시된 순서로 존재
- [ ] `## 상태`가 `채택 (2026-09-18)`
- [ ] `## 검토한 선택지`에 2개 이상, **(1) 수량 컬럼이 합리적인 경우가 명시적으로 서술됨**
- [ ] `## 근거`가 I-1, I-2, B-1~B-5, P-8을 참조
- [ ] `## 포기한 것`에 4개 항목 (조회 복잡도 / 중복 반환 / 저장 비용 / 2단계 등록)
- [ ] `## 재검토 조건`이 "되돌리기 어려움"과 "확장은 열림" 양쪽을 담고, 구체적 조건으로 서술
- [ ] `## 관련 문서`에 ADR-0007 링크와 규칙 번호
- [ ] 코드 블록 0개
- [ ] 규칙을 재서술하지 않음 (번호 참조만)
- [ ] 한 페이지 분량 (본문 100줄 이내 목표)
- [ ] `docs/adr/0007-*.md` 무변경

---

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| ADR 형식은 프로젝트에 하나뿐 | ADR-0007이 다른 6섹션 구조를 씀 | 0001만 새 형식으로 작성, 0007 미변경 (불일치를 알고 남김) |
| 인용된 규칙 번호가 실재할 것 | 확인 없이 쓰면 죽은 참조가 됨 | I-1·I-2·B-1~B-5·P-8·UC-08·UC-09·6장·§4.2 **12건 전수 검증 완료** |
| 예시 지점명은 문서와 맞춰야 함 | `강남점`이 네 문서 어디에도 없음 | 사용자 확정: 그대로 사용. ADR은 독립적으로 읽히는 문서 |

---

## Technical Context

- `docs/adr/`에는 현재 `0007-overdue-as-calculation.md` 1건만 존재. 0001~0006은 비어 있다
- ADR-0007 섹션 구조: 배경 / 결정 / 결정 근거 / 고려했으나 선택하지 않은 대안 / 결과 / 후속 작업
- 참조 대상 문서 현황: `02-requirements.md` 규칙 37종(L 13, M 8, P 9, C 2, B 5), UC 23개
- `01-domain.md` 불변식 I-1 ~ I-8, 6장 "조회 관점의 구조적 특징"
- `03-api.md` §4.2 "필터의 성격 — 결과 단위와 조건 단위가 다르다"
- 엔티티 코드는 문서의 목표 상태를 아직 따라오지 않았으나, ADR은 설계 결정 기록이므로 영향 없음

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Book | core domain | isbn, title, author, publisher, publishedAt, description? | belongs to Category, has many BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| Loan | core domain | loanedAt, dueDate, status, closedAt?, extensionCount | belongs to User, belongs to BookCopy |
| Library | supporting | name, address, phone | has many BookCopy, has many User(사서) |
| User | core domain | email, password, name, role, status, library? | belongs to Library (nullable), has many Loan |
| Category | supporting | name, parent? | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |

엔티티 집합 불변. 이 ADR은 새 개념을 만들지 않고 **이미 확정된 분리 결정을 기록**한다.

---

## Interview Transcript

<details>
<summary>Full Q&A (2 rounds)</summary>

### Round 0 — Topology
**Q:** 토폴로지(ADR 1건)가 맞는가? 기존 ADR-0007과의 형식 불일치(6섹션 vs 8섹션)를 어떻게 할 것인가?
**A:** 0001만 작성, 0007은 그대로.
**Ambiguity:** 8.0%

### Round 1 — Constraint Clarity
**Q:** 예시의 `강남점`이 네 문서 어디에도 없다 (03-api.md는 중앙도서관/동부분관/서부분관 사용). 어떻게 할 것인가?
**A:** 주신 대로 `강남점` 사용.
**Ambiguity:** 6.3%

**인터뷰 중 수행한 검증 (질문 대신):** 사용자가 인용한 참조 12건(I-1, I-2, B-1~B-5, P-8, UC-08, UC-09, 01-domain 6장, 03-api §4.2)이 모두 실재함을 스크립트로 확인.

</details>
