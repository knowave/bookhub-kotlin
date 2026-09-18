# Deep Interview Spec: docs/02-requirements.md 작성

## Metadata
- Interview ID: di-02req-20260918
- Rounds: 3 (Round 0 topology + 2 scoring rounds)
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
| 인증/회원 | active | 가입·로그인·사서 승인 | UC-01 ~ UC-05 |
| 도서 카탈로그 | active | 검색·상세·도서/복본 등록·복본 상태 | UC-06 ~ UC-10 |
| 대출 | active | 대출·반납·연장·이력/현황 조회 | UC-11 ~ UC-16 |
| 운영/마스터 데이터 | active | 지점·카테고리 관리 | UC-17 ~ UC-20 |

## Goal
docs/CONTEXT.md를 단일 출처로 삼아 docs/02-requirements.md를 작성한다.
5개 섹션(서비스 개요 / 액터 / 유스케이스 표 / 도메인 규칙+근거 / 범위 제외)을 갖추고,
CONTEXT에 없는 정보는 추측하지 않고 인터뷰로 확정한 뒤 반영한다.

## Constraints
- 한국어 작성, 표·목록 중심, 코드 복붙 금지
- 규칙마다 "왜"를 한 줄 근거로 병기
- CONTEXT.md에 없는 정보는 추측 금지 → 인터뷰로 해소
- UC 최소 10종 포함, 추가분은 추가 이유 명시
- 산출물 경로 고정: docs/02-requirements.md, docs/adr/0007-*.md

## Non-Goals
- 구현/API 설계 (별도 문서)
- 소스 코드 수정 (LoanStatus.OVERDUE 제거는 후속 작업으로 분리)

## Acceptance Criteria
- [x] 5개 섹션 모두 존재
- [x] UC-01 ~ UC-20, 도메인별 4개 그룹 표
- [x] 요구된 10종 UC 전부 포함
- [x] 추가 UC 6건의 추가 이유 명시
- [x] 대출 10 / 회원 5 / 권한 7 규칙에 근거 1줄씩
- [x] 범위 제외 7항목 + 이유
- [x] ADR-0007 작성
- [x] CONTEXT.md [대출]/[권한]/[마스터 데이터]/[범위 제외] 갱신

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| UC는 요청된 10종이면 충분 | 승인 대상 조회 없이 사서 승인이 가능한가? | UC-04 추가 (+ UC-07/10/14/16/17~20) |
| Library/Category는 알아서 생긴다 | 누가 생성하는가? | 개설=ADMIN, 지점정보수정=LIBRARIAN, 카테고리=ADMIN |
| LoanStatus.OVERDUE가 쓰인다 | 코드상 도달 불가 + isOverdue()와 모순 | OVERDUE 제거, 계산으로 통일 (ADR-0007) |
| 연장은 사서 업무다 | 실물 이동이 없는데 왜 창구여야 하는가? | 본인 또는 사서, 본인 시 소유권 검증 필수 |

## Technical Context
- Kotlin 2.3 / Spring Boot 4.1 / JPA / PostgreSQL / Kotest / JWT
- 엔티티 7종 구현 중 (미커밋): BaseEntity, User, Book, Category, Library, BookCopy, Loan
- `Loan.kt`: isOverdue()/overdueDays() 계산 로직 존재, OVERDUE enum 미사용,
  idx_loans_overdue (status, due_date) 인덱스 존재

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| User | core domain | email, password, name, role, status | has many Loan |
| Book | core domain | isbn, title, author, publisher, publishedAt, description | belongs to Category, has many BookCopy |
| BookCopy | core domain | callNumber, status | belongs to Book, belongs to Library, has many Loan |
| Loan | core domain | loanedAt, dueDate, status, returnedAt, extensionCount | belongs to User, belongs to BookCopy |
| Library | supporting | name, address, phone | has many BookCopy |
| Category | supporting | name, parent | self-referencing, has many Book |
| BaseEntity | supporting | id(UUID), createdAt, updatedAt | 전 엔티티 상속 |

## Ontology Convergence
| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 0 | 7 | 7 | - | - | N/A |
| 1 | 7 | 0 | 0 | 7 | 100% |
| 2 | 7 | 0 | 0 | 7 | 100% |

## Interview Transcript
<details>
<summary>Full Q&A (3 rounds)</summary>

### Round 0 — Topology
**Q:** 운영/마스터 데이터(Library 등록, Category 등록, 복본 상태 변경)를 1차 범위 UC로 포함할까?
**A:** Library 개설=ADMIN / 정보 수정=LIBRARIAN, Category 생성·수정=ADMIN. 각각 근거 제시.
**Ambiguity:** 16.3%

### Round 1 — Constraint Clarity (대출)
**Q:** OVERDUE를 상태로 저장할 것인가 계산할 것인가? (코드상 모순 제시)
**A:** 계산으로만 처리. OVERDUE enum 제거, 배치 없음, DTO에 isOverdue/overdueDays. CONTEXT 갱신 + ADR-0007 작성 요청.
**Ambiguity:** 9.2%

### Round 2 — Constraint Clarity (권한 귀속)
**Q:** 권한 미명시 행위 2건(대출 연장, 복본 상태 변경)의 귀속은?
**A:** 연장=본인+사서 대행, 복본 상태 변경=LIBRARIAN. 누적 권한 3단계 전체 재정의.
**Ambiguity:** 6.3%

</details>
