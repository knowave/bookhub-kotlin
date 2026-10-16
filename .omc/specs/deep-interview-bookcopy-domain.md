# Deep Interview Spec: BookCopy 도메인 (UC-09 / 10 / 10-1 / 10-2)

## Metadata
- Interview ID: `di-bookcopy-20261016`
- Rounds: 2 (+ Round 0 토폴로지)
- Final Ambiguity Score: **7.5%**
- Type: brownfield
- Generated: 2026-10-16
- Threshold: 0.2
- Threshold Source: `default`
- Initial Context Summarized: no
- Status: **PASSED**
- 선행 스펙: `.omc/specs/deep-interview-book-and-bookcopy-domains.md` (Book 단계 완료. BookCopy 항목 AC-C1~C21은 **이 스펙이 대체**한다)

## Clarity Breakdown

| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.95 | 0.35 | 0.333 |
| Constraint Clarity | 0.90 | 0.25 | 0.225 |
| Success Criteria | 0.90 | 0.25 | 0.225 |
| Context Clarity | 0.95 | 0.15 | 0.143 |
| **Total Clarity** | | | **0.925** |
| **Ambiguity** | | | **0.075** |

## Topology

| Component | Status | Description | Coverage / Deferral Note |
|-----------|--------|-------------|--------------------------|
| `book-copy` | active | BookCopy 도메인 전체 — Repository / Service / DTO / Controller / 예외 | AC-1 ~ AC-24 |
| `schema-fix` | active | `callNumber` 유일성을 지점 내로 교정 — 엔티티 인덱스 + V2 마이그레이션 | AC-25 ~ AC-26 |

Round 0에서 `schema-fix`를 제외했다가 **스펙 승인 단계에서 사용자가 번복**해 다시 포함했다. 현실에서 지점마다 독립적으로 청구기호를 매기므로 복합 unique가 맞다는 판단이다. 이에 따라 "엔티티 수정 금지"와 "Flyway 추가 금지"가 **이 두 파일에 한해** 해제된다.

## Goal

이미 존재하는 `domains/library/entity/BookCopy.kt` 위에 서비스·컨트롤러 계층을 올려 네 엔드포인트를 노출한다. BookCopy는 **library 도메인**에 속한다 (01-domain.md 2장 — 실물은 지점에 속하고 상태 변경이 지점의 행위다).

```
POST /api/v1/book-copies                     UC-09    복본 등록      201
POST /api/v1/book-copies/{id}/damage-report  UC-10    파손 처리      200
POST /api/v1/book-copies/{id}/repair         UC-10-1  수선 완료      200
POST /api/v1/book-copies/{id}/lost-report    UC-10-2  분실 처리      200
```

## Constraints

### `callNumber` 유일성 — **지점 내 유일로 교정** (스펙 승인 단계 확정)

현실에서 지점마다 독립적으로 청구기호를 매기므로 `(library_id, call_number)` 복합 unique가 맞다. 현재 스키마의 전역 unique를 **버그로 판정하고 교정한다.** 교정 후 코드·문서·컬럼 주석이 모두 일치한다.

| 출처 | 현재 | 교정 후 |
|---|---|---|
| `V1__init_schema.sql:93` | `constraint uk_copies_call_number unique (call_number)` | V2가 `(library_id, call_number)`로 재정의 |
| `BookCopy.kt:20` | `Index(columnList = "call_number", unique = true)` | `columnList = "library_id, call_number"` |
| `docs/03-api.md:439` | "지점 내 유일" | **이미 맞음 — 수정 없음** |
| `V1:100` 컬럼 주석 | "지점 내에서 실물을 식별하는 값" | **이미 맞음 — 수정 없음** |

**강제 수단은 V2 하나다.** 확인한 사실: `build.gradle.kts:36`에 `spring-boot-starter-flyway`가 있고 `application.yaml:20`은 `ddl-auto: validate`다. 스키마의 주인은 Flyway이며, Hibernate의 `validate`는 테이블·컬럼·타입만 검사하고 **인덱스와 unique 제약은 검사하지 않는다.** 따라서 엔티티 `@Index` 변경은 런타임 효과가 없고 "엔티티가 스키마를 정직하게 기술한다"는 의미만 갖는다. 그래도 고친다 — 어긋난 채 두면 다음 읽는 사람이 속는다.

**미해결로 남기는 것:** 엔티티 인덱스 이름(`idx_copies_call_number`)과 DDL 제약 이름(`uk_copies_call_number`)이 **원래부터 다르다.** 이번 작업이 만든 문제가 아니고 `validate`에서는 드러나지 않으므로 건드리지 않는다. 다만 `ddl-auto`를 `update`/`create`로 바꾸면 Hibernate가 별도 인덱스를 하나 더 만들게 된다.

### 엔드포인트 분리 — HTTP 표면만

상태 변경을 `PATCH` 하나로 묶지 않는다. 컨트롤러는 행위별 `POST` 4개를 유지한다. **단, 서비스는 단일 메서드다** (Round 1 확정) — 이 둘은 층이 다르므로 충돌하지 않는다.

> **정정 기록:** 인터뷰 중 "단일 서비스 메서드가 ADR-0008과 어긋난다"고 경고했으나 **틀렸다.** ADR-0008은 *어떤 전이가 허용되는가*만 규정하며 엔드포인트 형태·PATCH·메서드 분해를 언급하지 않는다. 엔드포인트 분리 근거는 `docs/03-api.md`(`/contact`·`/name` 분리와 같은 계열)에 있다.

### 상태 전이 (B-1 ~ B-4)

허용 전이는 B-1이 단일 출처다. 이번 범위에서 서비스가 유발하는 전이는 셋뿐이다 (`AVAILABLE→LOANED`, `LOANED→AVAILABLE`은 Loan 도메인 소관):

| 행위 | 허용 시작 상태 | 결과 | 엔티티 메서드 |
|---|---|---|---|
| 파손 | `AVAILABLE`만 | `DAMAGED` | `markAsDamaged()` |
| 수선 | `DAMAGED`만 | `AVAILABLE` | `repair()` |
| 분실 | `AVAILABLE` 또는 `LOANED` | `LOST` | `markAsLost()` |

파손이 `AVAILABLE`에서만 가능한 이유는 B-4 — 파손된 책은 실물이 돌아오므로 반납이 선행 가능하다. 분실이 `LOANED`에서도 가능한 이유는 분실된 책은 반납 자체가 불가능하기 때문이다.

### 예외 처리 방식 — 서비스 선검사

엔티티의 `check`는 `IllegalStateException`을 던진다. **그대로 둔다** — 정상 경로에서 도달하지 않는 불변식 가드다. 서비스가 상태를 먼저 확인하고 도메인 예외(409)를 던져 `check`에 닿지 않게 한다. `UserServiceImpl.approveLibrarian`(`UserStatus.PENDING` 선검사), `CategoryServiceImpl.updateCategory`(`isSelfOrDescendantOf` 선검사)와 같은 방식이다.

### 그 밖

- `BookCopyService`는 `BookService`·`LibraryService`를 주입한다. `BookRepository`·`LibraryRepository` 직접 주입 금지.
- 구체 예외는 `domains/library/exception/`에 둔다 (프로젝트 메모리 directive).
- 단방향 연관관계만 (ADR-0005).
- 권한 검사는 구현하지 않고 TODO 주석만.
- 조회 후 값만 바꾸는 경우 `save()`를 호출하지 않는다 (더티 체킹).

## Non-Goals

- **B-5** (대출 중 복본 분실 시 해당 대출도 LOST로 종료) — Loan 도메인 없음. TODO 주석만
- 대출/반납 (`loanOut()` / `returned()`를 호출하는 경로) — Loan 도메인 작업
- 복본 조회·목록 엔드포인트 (문서에 없음)
- 권한 검증 실제 구현, `SecurityConfig` 변경
- 테스트 코드
- `docs/03-api.md` 수정 — **0건**

## Acceptance Criteria

### 리포지토리
- [ ] AC-1: `domains/library/repository/BookCopyRepository.kt` — `JpaRepository<BookCopy, UUID>` + `fun existsByLibraryIdAndCallNumber(libraryId: UUID, callNumber: String): Boolean`. 파생 쿼리 프로퍼티 경로는 `library.id` — `BookCopy.library`가 `@ManyToOne`이므로 해석된다

### 예외 — `domains/library/exception/BookCopyExceptions.kt` (신규 파일, `LibraryExceptions.kt`와 같은 패키지)
- [ ] AC-2: `BookCopyNotFoundException(bookCopyId: UUID)` — 404, `BOOK_COPY_NOT_FOUND`, rule 없음
- [ ] AC-3: `BookCopyCallNumberDuplicatedException(callNumber: String)` — 409, `BOOK_COPY_CALL_NUMBER_DUPLICATED`, rule 없음
- [ ] AC-4: `BookCopyLoanedCannotDamageException(bookCopyId: UUID)` — 409, `BOOK_COPY_LOANED_CANNOT_DAMAGE`, **rule `B-4`**
- [ ] AC-5: `BookCopyNotDamagedException(bookCopyId: UUID)` — 409, `BOOK_COPY_NOT_DAMAGED`, **rule `B-2`**
- [ ] AC-6: `BookCopyInvalidTransitionException(bookCopyId: UUID, status: BookCopyStatus)` — 409, `BOOK_COPY_INVALID_TRANSITION`, **rule `B-1`**. 메시지에 현재 상태의 `label`을 넣는다

### 서비스 DTO — `domains/library/service/dto/`
- [ ] AC-7: `CreateBookCopyCommand.kt` — `bookId: UUID`, `libraryId: UUID`, `callNumber: String`
- [ ] AC-8: `ChangeBookCopyStatusCommand.kt` — `bookCopyId: UUID`, `action: BookCopyAction`. **`BookCopyAction` enum을 같은 파일에 둔다** (`DAMAGE_REPORT`, `REPAIR`, `LOST_REPORT`) — 이 커맨드 밖에서 쓰이지 않는다
- [ ] AC-9: `BookCopyResult.kt` — `id`, `callNumber`, `status: BookCopyStatus`, `book: BookRef`, `library: LibraryRef` + `from(bookCopy: BookCopy)`. `id`는 `checkNotNull(...) { "영속화되지 않은 BookCopy는 변환할 수 없습니다" }`

### 참조 타입 — book 도메인 (`LibraryRef`가 library 도메인에 사는 것과 같은 배치)
- [ ] AC-10: `domains/book/service/dto/BookRef.kt` — `id: UUID`, `title: String` + `from(book: Book)`. 문서 `:446`의 `book: { id, title }`에 대응
- [ ] AC-11: `domains/book/controller/dto/BookRefResponse.kt` — `from(ref: BookRef)`
- [ ] AC-12: **`LibraryRef`는 재사용한다.** `domains/library/service/dto/LibraryRef.kt`가 이미 `id`+`name`을 갖고 `LibraryRefResponse`도 있다. 새로 만들지 않는다

### 서비스
- [ ] AC-13: `domains/library/service/BookCopyService.kt` — 인터페이스, 세 메서드 **전부 KDoc**:
      `createBookCopy(command: CreateBookCopyCommand): BookCopyResult` (UC-09),
      `changeStatus(command: ChangeBookCopyStatusCommand): BookCopyResult` (UC-10 / 10-1 / 10-2),
      `getBookCopyEntity(bookCopyId: UUID): BookCopy` — "대출 생성(UC-11)에서 연관관계를 맺기 위해 엔티티를 반환한다"
- [ ] AC-14: `domains/library/service/BookCopyServiceImpl.kt` — `@Service` + 클래스 `@Transactional(readOnly = true)`, 쓰기 메서드 2개만 `@Transactional`. 주입은 `BookCopyRepository`, `BookService`, `LibraryService` **셋뿐**
- [ ] AC-15: `createBookCopy` 순서 — `existsByLibraryIdAndCallNumber(command.libraryId, command.callNumber)` 선검사 → 409, 그다음 `bookService.getBookEntity(command.bookId)`, `libraryService.getLibraryEntity(command.libraryId)`(각각 404 전파), `BookCopy(...)` 생성 후 `save`
- [ ] AC-16: `changeStatus`는 `when (command.action)` 하나로 분기하고, **각 분기가 선검사 후 엔티티 메서드를 호출**한다:
      - `DAMAGE_REPORT`: `LOANED`면 `BookCopyLoanedCannotDamageException`, `AVAILABLE`이 아니면 `BookCopyInvalidTransitionException`, 통과 시 `markAsDamaged()`
      - `REPAIR`: `DAMAGED`가 아니면 `BookCopyNotDamagedException`, 통과 시 `repair()`
      - `LOST_REPORT`: `AVAILABLE`도 `LOANED`도 아니면 `BookCopyInvalidTransitionException`, 통과 시 `markAsLost()`
- [ ] AC-17: `changeStatus`에 `save()` 호출 없음 (더티 체킹)
- [ ] AC-18: `LOST_REPORT` 분기에 B-5 TODO — `// TODO: 대출 중 복본 분실 시 해당 대출도 LOST로 종료해야 한다 (→ B-5). Loan 도메인 구현 시 추가`

### 컨트롤러 DTO — `domains/library/controller/dto/`
- [ ] AC-19: `CreateBookCopyRequest.kt` — `bookId`, `libraryId`, `callNumber` + `toCommand()`. `callNumber`에 `@field:NotBlank` + `@field:Size(max = 50)`(컬럼 길이와 일치), 메시지는 기존 형식
- [ ] AC-20: `BookCopyResponse.kt` — 등록 응답. 문서 `:446` 그대로 `id`, `callNumber`, `status`, `book: BookRefResponse`, `library: LibraryRefResponse`
- [ ] AC-21: `BookCopyStatusResponse.kt` — 파손·수선 응답. 문서 `:476`·`:502` 그대로 `id`, `callNumber`, `status` **셋뿐**
- [ ] AC-22: `BookCopyLostResponse.kt` — 분실 응답. 문서 `:527` 그대로 `id`, `callNumber`, `status`, `closedLoan`. **`closedLoan`은 `Nothing?` 타입이며 항상 `null`** — B-5 미구현이라 다른 값이 들어갈 수 없음을 타입으로 고정한다. KDoc 한 줄로 이유를 남긴다

### 컨트롤러
- [ ] AC-23: `domains/library/controller/BookCopyController.kt` — `@RequestMapping("/api/v1/book-copies")`, 생성자 주입. 등록만 `@ResponseStatus(HttpStatus.CREATED)`, 나머지 셋은 기본 200. 전이 3개는 `@PathVariable id: UUID`만 받고 `@RequestBody` 없음. 각각 `ChangeBookCopyStatusCommand(id, <ACTION>)`을 만들어 호출
- [ ] AC-24: 네 엔드포인트 모두 `// TODO: LIBRARIAN 권한 검증 필요 (→ P-8). JWT 도입 시 추가`

### Component `schema-fix` (금지 해제 대상 — 이 두 파일만)
- [ ] AC-25: `domains/library/entity/BookCopy.kt:20` — `Index(name = "idx_copies_call_number", columnList = "call_number", unique = true)`의 `columnList`를 `"library_id, call_number"`로 바꾼다. **이 한 줄 외에는 아무것도 건드리지 않는다** (인덱스 이름, `idx_copies_lookup`, 프로퍼티, 전이 메서드 전부 그대로)
- [ ] AC-26: `src/main/resources/db/migration/V2__fix_call_number_scope.sql` 신설:
      ```sql
      alter table book_copies drop constraint uk_copies_call_number;
      alter table book_copies add constraint uk_copies_call_number
          unique (library_id, call_number);
      ```
      제약 이름은 `uk_copies_call_number`를 **유지**한다 — V1과 같은 이름이라 히스토리를 따라가기 쉽다

### 전체
- [ ] AC-Z1: `./gradlew compileKotlin compileTestKotlin` → `BUILD SUCCESSFUL`
- [ ] AC-Z2: 기존 단위 테스트 11건 유지 통과 (`AuthServiceUnitTest` 3, `UserServiceUnitTest` 8)
- [ ] AC-Z3: `src/test` 변경 0건, `docs/` 변경 0건. 엔티티 변경은 **`BookCopy.kt`의 `columnList` 한 줄만**, 마이그레이션 추가는 **`V2__fix_call_number_scope.sql` 하나만**
- [ ] AC-Z4: `common/exception`에 새 파일 0건 (예외는 전부 `domains/library/exception/`)

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "callNumber unique" (범위 미지정) | 선행 인터뷰는 지점 내 유일 + 엔티티/Flyway 변경 승인, 이번 프롬프트는 둘 다 금지 | Round 0에서 전역 유일로 정했다가 **스펙 승인 단계에서 지점 내 유일로 번복.** 금지 2건을 두 파일에 한해 해제 |
| "V2를 추가하면 제약이 바뀐다" | Flyway가 실제로 도는가? `ddl-auto`는? | **돈다.** `spring-boot-starter-flyway` + `ddl-auto: validate`. 스키마 주인은 Flyway이고 `validate`는 인덱스·unique를 검사하지 않으므로 **엔티티 `@Index` 변경은 런타임 효과가 없다** |
| "id는 Command에 포함한다" | body 없는 전이 3개에도 적용되는가? `approveLibrarian`은 bare UUID 선례 | **전이 3개를 단일 `changeStatus(command)`로 통합.** `BookCopyAction` enum으로 분기 (Round 1) |
| "단일 서비스 메서드는 ADR-0008 위반" | ADR-0008 원문을 읽음 | **위반 아님.** ADR-0008은 허용 전이만 규정하고 엔드포인트·메서드 분해를 언급하지 않는다. 경고가 틀렸고 스펙에 정정을 남김 |
| "문서의 응답 스키마를 만들 수 있다" | `closedLoan`은 Loan 도메인 없이 값을 만들 수 없음 | **응답 3종 유지, `closedLoan`은 항상 null.** 문서 수정 0건. Book 단계의 `totalCopies = 0`과 같은 방침 (Round 2) |
| "BookCopy 엔티티를 만들어야 한다" | 이미 있는가? | **있다.** 전이 메서드 5종(`loanOut`/`returned`/`markAsDamaged`/`markAsLost`/`repair`)과 `BookCopyStatus` enum 4값까지 완비 |
| "LibraryRef를 새로 만든다" | 이미 있는가? | **있다.** `domains/library/service/dto/LibraryRef.kt` + `LibraryRefResponse`. 재사용. `BookRef`만 신규 |
| "예외는 common/exception" | 2026-10-15 directive 변경 | **`domains/library/exception/`.** `BookExceptions`/`CategoryExceptions`가 `domains/book/exception/`에 있는 것과 같은 배치 |

## Technical Context

**이미 존재 (건드리지 않음)**

| 파일 | 내용 |
|---|---|
| `domains/library/entity/BookCopy.kt` | `callNumber`/`book`/`library`(val), `status`(protected set), `isLoanable`, 전이 5종, `BookCopyStatus(label)` 4값 |
| `domains/library/service/dto/LibraryRef.kt` | `id`+`name`. **재사용 대상** |
| `domains/library/controller/dto/LibraryRefResponse.kt` | `from(ref: LibraryRef)`. **재사용 대상** |
| `domains/library/exception/LibraryExceptions.kt` | `LibraryNotFoundException`. 새 파일을 같은 패키지에 추가 |
| `domains/book/service/BookService.kt` | `getBookEntity(bookId): Book` — 진입점 |
| `domains/library/service/LibraryService.kt` | `getLibraryEntity(libraryId): Library` — 진입점 |
| `common/exception/GlobalExceptionHandler.kt` | `BusinessException` → 상태코드 자동. **에러 처리 코드 추가 불필요** |
| `db/migration/V1__init_schema.sql:93` | `uk_copies_call_number unique (call_number)` — 전역. 교정하지 않음 |

**따라야 할 패턴 — 파일로 지목**

| 패턴 | 참조 |
|---|---|
| Ref 타입 (service / controller) | `domains/library/service/dto/LibraryRef.kt` / `controller/dto/LibraryRefResponse.kt` |
| Result에 Ref 담기 | `domains/book/service/dto/BookResult.kt` (`CategoryRef.from(book.category)`) |
| 중복 검사 → 예외 | `domains/book/service/BookServiceImpl.kt` (`existsByIsbn` → `BookIsbnDuplicatedException`) |
| 상태 선검사 → 도메인 예외 | `domains/user/service/UserServiceImpl.kt:66-67` |
| 서비스 인터페이스 KDoc | `domains/book/service/BookService.kt` |
| 트랜잭션 배치 | `domains/library/service/LibraryServiceImpl.kt:16,25` |
| 컨트롤러 한 줄 위임 + 권한 TODO | `domains/book/controller/BookController.kt` |
| 요청 DTO 검증 | `domains/book/controller/dto/CreateBookRequest.kt` |

**주의점**
- `open-in-view: false`. 컨트롤러는 Result/Response만 다루고 엔티티를 받지 않는다. `BookCopyResult.from`이 `book`·`library` 지연 로딩을 트리거하므로 **반드시 `@Transactional` 안에서** 호출돼야 한다 — 서비스 메서드 내부이므로 충족된다.
- `libraryId`를 요청 body로 받는다 (문서 `:440`). JWT 도입 시 토큰에서 가져오도록 바뀐다.

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| `BookCopy` | core domain (기존) | `callNumber`, `status`, `book`, `library` | N:1 `Book`, N:1 `Library` |
| `BookCopyStatus` | core domain (기존) | `AVAILABLE`/`LOANED`/`DAMAGED`/`LOST` + `label` | `BookCopy`가 보유 |
| `BookCopyAction` | supporting (신규) | `DAMAGE_REPORT`/`REPAIR`/`LOST_REPORT` | `ChangeBookCopyStatusCommand`가 보유 |
| `BookRef` | supporting (신규) | `id`, `title` | `Book`의 얕은 참조. `BookCopyResult`가 보유 |
| `LibraryRef` | supporting (기존, 재사용) | `id`, `name` | `BookCopyResult`가 보유 |
| `BookCopyResult` | supporting (신규) | `id`, `callNumber`, `status`, `book`, `library` | ← `BookCopy` |
| `ChangeBookCopyStatusCommand` | supporting (신규) | `bookCopyId`, `action` | → `changeStatus` |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 7 | 7 | - | - | N/A |
| 2 | 7 | 0 | 0 | 7 | 100% |

## Interview Transcript

<details>
<summary>Full Q&A (Round 0 + 2 rounds)</summary>

### Round 0 — 토폴로지 확인
**Q:** `callNumber` 유일성이 별도 컴포넌트를 만드는가? 선행 인터뷰는 지점 내 유일 + 엔티티/Flyway 변경을 승인했으나 이번 프롬프트는 둘 다 금지한다.
**A:** **전역 유일 — 스키마대로, 금지 사항 준수.** 컴포넌트 1개.
**Ambiguity:** 미측정

### Round 1 — Constraint Clarity
**Q:** body 없는 전이 3개의 서비스 시그니처는? (bare UUID / Command 3개 / 단일 Command)
**A:** **전이 3개를 한 Command로.** `changeStatus(ChangeBookCopyStatusCommand)` + `BookCopyAction` enum.
**Ambiguity:** 15.7% → 12.4% (Goal 0.94, Constraints 0.82, Criteria 0.80, Context 0.95)

### Round 2 — Success Criteria
**Q:** 응답 DTO 3종 중 `closedLoan`을 어떻게 할까?
**A:** **문서대로 3종 + `closedLoan`은 null 고정.** 문서 수정 0건.
**Ambiguity:** 12.4% → **7.5%** (Goal 0.95, Constraints 0.90, Criteria 0.90, Context 0.95)

### 스펙 승인 단계 — Round 0 번복
**사용자:** "현실에서는 지점마다 독립적으로 청구기호를 매기므로 복합 unique가 맞다. V2 마이그레이션에서 교정한다. 엔티티의 `@Table` indexes도 함께 수정해야 한다."
**결과:** `schema-fix` 컴포넌트 복원, AC-1/AC-15 변경, AC-25/26 신설. 금지 2건을 두 파일에 한해 해제.

</details>

## Open Risks

1. **`closedLoan: Nothing?`이 런타임에서 검증되지 않는다.** `Nothing?`은 null만 담을 수 있어 의도를 타입으로 고정하지만, Jackson이 이 프로퍼티를 직렬화하는 동작은 컴파일로 확인되지 않는다. 응답 전용이라 역직렬화 문제는 없고 JVM 시그니처가 `Void`로 내려가 null 직렬화는 정상일 것으로 보이나, **실제 호출로 확인하지 못했다.** 문제가 생기면 `Any?`로 한 줄 교체하면 된다.
2. **V2는 기존 데이터에 적용되는 마이그레이션이다.** 전역 unique → 복합 unique는 **제약 완화** 방향이라 기존 행이 깨지지는 않는다. 다만 운영 DB에 이미 데이터가 있다면 `drop constraint` 후 `add constraint` 사이에 짧은 잠금이 생긴다. 로컬·개발 환경에서는 무해하다.
3. **`BookCopyAction` enum은 문서에 없는 개념이다.** 도메인 규칙이 아니라 서비스 분기용 구현 장치이므로 "문서에 없는 규칙 금지"에 걸리지 않지만, 전이가 추가되면 enum·`when`·엔드포인트 세 곳을 같이 고쳐야 한다. 컴파일러가 `when` 누락은 잡아주지만 엔드포인트 누락은 잡지 못한다.

---

**Status: pending approval**
