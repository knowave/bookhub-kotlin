# Deep Interview Spec: Book(UC-08) · BookCopy(UC-09/10/10-1/10-2) 도메인 구현

## Metadata
- Interview ID: `di-book-bookcopy-20261015`
- Rounds: 2 (+ Round 0 토폴로지)
- Final Ambiguity Score: **13.2%**
- Type: brownfield
- Generated: 2026-10-15
- Threshold: 0.2
- Threshold Source: `default`
- Initial Context Summarized: no
- Status: **PASSED**

## Clarity Breakdown

| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.93 | 0.35 | 0.326 |
| Constraint Clarity | 0.82 | 0.25 | 0.205 |
| Success Criteria | 0.78 | 0.25 | 0.195 |
| Context Clarity | 0.95 | 0.15 | 0.143 |
| **Total Clarity** | | | **0.868** |
| **Ambiguity** | | | **0.132** |

## Topology

| Component | Status | Description | Coverage / Deferral Note |
|-----------|--------|-------------|--------------------------|
| `book` | active | Book 도메인 UC-08 (등록). Repository/Service/Impl/DTO/Controller + `CategoryRef` | AC-B1 ~ AC-B14 |
| `book-copy` | active | BookCopy 도메인 UC-09/10/10-1/10-2. library 도메인에 속함 | AC-C1 ~ AC-C21 |
| `api-doc` | active | `docs/03-api.md` 대조 | **수정 0건 예상.** 라운드 1·2 결정이 전부 문서와 일치하는 방향으로 났다. AC-D1 ~ AC-D3은 대조 검증만 |

**진행 순서:** `book`을 완료하고 **멈춰서 보고**한다. 사용자 확인 후 `book-copy`로 넘어간다. `api-doc` 대조는 각 라운드 끝에 수행한다.

## Goal

이미 존재하는 `Book`·`BookCopy` 엔티티 위에, `domains/library`(가장 성숙)와 `domains/book`(Category, 가장 최근)이 쓰는 패턴 그대로 서비스·컨트롤러 계층을 올린다. UC-08 도서 등록과 UC-09/10/10-1/10-2 복본 등록·상태 전이 다섯 엔드포인트를 노출한다.

```
POST /api/v1/books                           UC-08    LIBRARIAN (소속 무관 → P-9)
POST /api/v1/book-copies                     UC-09    LIBRARIAN (소속 일치 → P-8)
POST /api/v1/book-copies/{id}/damage-report  UC-10    LIBRARIAN (소속 일치 → P-8)
POST /api/v1/book-copies/{id}/repair         UC-10-1  LIBRARIAN (소속 일치 → P-8)
POST /api/v1/book-copies/{id}/lost-report    UC-10-2  LIBRARIAN (소속 일치 → P-8)
```

## Constraints

### 전역
- 테스트 코드를 작성하지 않는다.
- 문서에 없는 도메인 규칙을 만들어내지 않는다.
- 권한 검사는 구현하지 않고 TODO 주석만 남긴다.
- 단방향 연관관계만 사용한다 (ADR-0005).
- 다른 도메인 접근은 **서비스 인터페이스**를 통한다. `BookCopyService`는 `BookService`·`LibraryService`를 주입받고 `BookRepository`·`LibraryRepository`를 직접 주입하지 않는다.
- `@Transactional(readOnly = true)`를 클래스에, 쓰기 메서드만 `@Transactional`로 덮는다.
- 조회 후 값만 바꾸는 경우 `save()`를 호출하지 않는다 (더티 체킹).
- `checkNotNull`로 id를 확정하고 메시지에 이유를 남긴다.
- 도메인 예외는 `BusinessException`을 상속하고 `code`/`status`/`rule`을 갖는다.

### `callNumber` 유일성 — Round 1 확정
**지점 내 유일**로 확정한다. 현재 스키마 제약(`V1__init_schema.sql:93` `unique (call_number)`)과 엔티티 인덱스(`BookCopy.kt:21`)가 **전역 유일**이라 컬럼 주석(`V1:100` "지점 내에서")·API 문서(`03-api.md:439` "지점 내 유일")와 어긋나 있다. 이를 **스키마 버그로 판정**하고 바로잡는다.

이 결정에 한해 두 가지 금지가 해제된다:
- `BookCopy.kt`의 unique index를 `columnList = "library_id, call_number"` 복합으로 변경 (**이 변경만** 허용. 다른 엔티티 변경은 여전히 금지)
- `V2__fix_call_number_scope.sql` 추가 (**이 마이그레이션만** 허용)

### 응답 필드 — Round 2 확정
문서의 응답 스키마를 그대로 유지하고 고정값을 쓴다. 문서 수정 0건.
- `BookResponse.totalCopies = 0` — 생성 직후에는 언제나 0이므로 거짓이 아니다.
- lost-report 응답의 `closedLoan = null` — **항상 null.** B-5 미구현.

### B-5 제외
대출 중 복본 분실 시 해당 대출도 LOST로 종료하는 규칙은 이번 범위 밖이다. Loan 도메인이 없다. `BookCopyServiceImpl`에 TODO 주석으로 남긴다.

### 상태 전이 처리 방식
엔티티의 `check`는 `IllegalStateException`을 던진다. **그대로 둔다** — 불변식 가드로 남긴다. 서비스가 상태를 **먼저 확인**하고 도메인 예외(409)를 던져 `check`에 도달하지 않게 한다. `UserServiceImpl.approveLibrarian`(`UserStatus.PENDING` 선검사 → `UserNotPendingException`)과 `CategoryServiceImpl.updateCategory`(`isSelfOrDescendantOf` 선검사 → `CategoryCircularReferenceException`)가 같은 방식이다.

### body 없는 상태 전이에 Command를 만들지 않는다
`damage-report`/`repair`/`lost-report`는 path `id`만 받고 body가 없다. `UserService.approveLibrarian(librarianId: UUID)` 선례를 따라 **bare `UUID`를 받는다.** "수정 대상의 id는 Command에 포함한다"는 규칙은 body가 있는 경우(`UpdateCategoryCommand` 등)에 적용된다.

## Non-Goals

- UC-06 도서 검색 (`GET /api/v1/books`), UC-07 도서 상세 (`GET /api/v1/books/{id}`)
- B-5 연쇄 (대출 중 복본 분실 → 대출 LOST 종료)
- 대출/반납 (`loanOut()` / `returned()`를 호출하는 서비스 경로) — Loan 도메인 작업
- 권한 검증 실제 구현 (`@PreAuthorize`, JWT, `SecurityConfig` 변경)
- 테스트 코드
- `BookCopy` 외 엔티티 수정, `V2` 외 마이그레이션 추가
- `totalCopies` 실제 계산 (UC-07에서 구현)

## Acceptance Criteria

### Component `book` (1단계 — 완료 후 멈추고 보고)

**신규 파일**
- [ ] AC-B1: `domains/book/repository/BookRepository.kt` — `JpaRepository<Book, UUID>` + `fun existsByIsbn(isbn: String): Boolean`
- [ ] AC-B2: `domains/book/service/dto/CategoryRef.kt` — `data class CategoryRef(id: UUID, name: String)` + `companion object { fun from(category: Category) }`, `checkNotNull` 메시지 포함. `LibraryRef`와 동일한 형태
- [ ] AC-B3: `domains/book/service/dto/CreateBookCommand.kt` — `isbn, title, author, publisher, publishedAt: LocalDate, categoryId: UUID, description: String?`
- [ ] AC-B4: `domains/book/service/dto/BookResult.kt` — `id, isbn, title, author, publisher, publishedAt, description: String?, category: CategoryRef, totalCopies: Int` + `fun from(book: Book, totalCopies: Int): BookResult`. **`totalCopies`에 기본값을 주지 않는다** — 호출부가 명시해야 "생성 직후라 0"이라는 근거가 그 자리에 남는다
- [ ] AC-B5: `domains/book/service/BookService.kt` — 인터페이스. `createBook(command): BookResult`, `getBookEntity(bookId): Book`. 두 메서드 모두 KDoc (유스케이스 / 예외)
- [ ] AC-B6: `domains/book/service/BookServiceImpl.kt` — `@Service` + 클래스 `@Transactional(readOnly = true)`, `createBook`만 `@Transactional`. `BookRepository`와 `CategoryService` 주입
- [ ] AC-B7: `domains/book/controller/dto/CategoryRefResponse.kt` — `LibraryRefResponse`와 동일 형태
- [ ] AC-B8: `domains/book/controller/dto/CreateBookRequest.kt` — `toCommand(): CreateBookCommand`
- [ ] AC-B9: `domains/book/controller/dto/BookResponse.kt` — **문서 `:413` 그대로** `id, isbn, title, category: CategoryRefResponse, totalCopies`. `author`/`publisher`/`publishedAt`/`description`은 담지 않는다
- [ ] AC-B10: `domains/book/controller/BookController.kt` — `@RequestMapping("/api/v1/books")`, `POST` + `@ResponseStatus(HttpStatus.CREATED)`
- [ ] AC-B11: `common/exception/BookExceptions.kt` — `BookIsbnDuplicatedException(isbn)` (409, `BOOK_ISBN_DUPLICATED`), `BookNotFoundException(bookId)` (404, `BOOK_NOT_FOUND`)

**동작**
- [ ] AC-B12: `createBook`이 `existsByIsbn`으로 먼저 검사하고 중복이면 `BookIsbnDuplicatedException`. `UserServiceImpl.createMember`의 `existsByEmail` → `EmailDuplicatedException`과 같은 순서
- [ ] AC-B13: `category`는 `categoryService.getCategoryEntity(command.categoryId)`로 조회한다. 없으면 `CategoryNotFoundException`이 그대로 전파된다 (잡지 않는다)
- [ ] AC-B14: 검증은 엔티티 컬럼 길이에 맞춘다 — `isbn` `@NotBlank`+`@Size(max=20)`, `title` `@NotBlank`+`@Size(max=300)`, `author` `@NotBlank`+`@Size(max=200)`, `publisher` `@NotBlank`+`@Size(max=100)`, `description` `@Size(max=2000)`. 메시지는 기존 DTO 형식(`"name은 필수값입니다."`)을 따른다
- [ ] AC-B15: 컨트롤러에 `// TODO: LIBRARIAN 권한 검증 필요 (→ P-3). 소속 지점은 검사하지 않는다 (→ P-9). JWT 도입 시 추가`

### Component `book-copy` (2단계 — 사용자 확인 후)

**엔티티·스키마 (이 둘만 허용)**
- [ ] AC-C1: `domains/library/entity/BookCopy.kt`의 `Index(name = "idx_copies_call_number", columnList = "call_number", unique = true)` → `columnList = "library_id, call_number"`. 다른 줄은 건드리지 않는다
- [ ] AC-C2: `src/main/resources/db/migration/V2__fix_call_number_scope.sql` — `uk_copies_call_number` drop 후 `unique (library_id, call_number)` 추가

**신규 파일**
- [ ] AC-C3: `domains/library/repository/BookCopyRepository.kt` — `JpaRepository<BookCopy, UUID>` + `fun existsByLibraryIdAndCallNumber(libraryId: UUID, callNumber: String): Boolean`
- [ ] AC-C4: `domains/book/service/dto/BookRef.kt` — `data class BookRef(id: UUID, title: String)` + `from(book: Book)`. 문서 `:446`의 `book: { id, title }`에 대응
- [ ] AC-C5: `domains/book/controller/dto/BookRefResponse.kt`
- [ ] AC-C6: `domains/library/service/dto/CreateBookCopyCommand.kt` — `bookId: UUID, libraryId: UUID, callNumber: String`
- [ ] AC-C7: `domains/library/service/dto/BookCopyResult.kt` — `id, callNumber, status: BookCopyStatus, book: BookRef, library: LibraryRef`
- [ ] AC-C8: `domains/library/service/BookCopyService.kt` — 인터페이스, 전 메서드 KDoc:
      `createBookCopy(command): BookCopyResult` (UC-09),
      `reportDamage(bookCopyId: UUID): BookCopyResult` (UC-10),
      `repair(bookCopyId: UUID): BookCopyResult` (UC-10-1),
      `reportLost(bookCopyId: UUID): BookCopyResult` (UC-10-2),
      `getBookCopyEntity(bookCopyId: UUID): BookCopy`
- [ ] AC-C9: `domains/library/service/BookCopyServiceImpl.kt` — `BookCopyRepository`, `BookService`, `LibraryService` 주입. **`BookRepository`/`LibraryRepository` 직접 주입 금지**
- [ ] AC-C10: `domains/library/controller/dto/CreateBookCopyRequest.kt`
- [ ] AC-C11: `domains/library/controller/dto/BookCopyResponse.kt` — 등록 응답. 문서 `:446` 그대로 `id, callNumber, status, book, library`
- [ ] AC-C12: `domains/library/controller/dto/BookCopyStatusResponse.kt` — damage-report / repair 응답. 문서 `:476`·`:502` 그대로 `id, callNumber, status`
- [ ] AC-C13: `domains/library/controller/dto/BookCopyLostResponse.kt` — lost-report 응답. 문서 `:527` 그대로 `id, callNumber, status, closedLoan`. **`closedLoan`은 항상 `null`**
- [ ] AC-C14: `domains/library/controller/BookCopyController.kt` — `@RequestMapping("/api/v1/book-copies")`, 엔드포인트 4개. 등록만 201, 나머지 200
- [ ] AC-C15: `common/exception/BookCopyExceptions.kt` — 5종:
      `BookCopyNotFoundException` (404, `BOOK_COPY_NOT_FOUND`),
      `BookCopyCallNumberDuplicatedException` (409, `BOOK_COPY_CALL_NUMBER_DUPLICATED`),
      `BookCopyLoanedCannotDamageException` (409, `BOOK_COPY_LOANED_CANNOT_DAMAGE`, rule `B-4`),
      `BookCopyNotDamagedException` (409, `BOOK_COPY_NOT_DAMAGED`, rule `B-2`),
      `BookCopyInvalidTransitionException` (409, `BOOK_COPY_INVALID_TRANSITION`, rule `B-1`)

**동작 — 상태 전이 분기 (문서 에러 표 그대로)**
- [ ] AC-C16: `createBookCopy` — `existsByLibraryIdAndCallNumber` 먼저 검사 → 중복이면 409. `book`은 `bookService.getBookEntity`, `library`는 `libraryService.getLibraryEntity`로 조회(각각 404 전파)
- [ ] AC-C17: `reportDamage` — `LOANED`이면 `BookCopyLoanedCannotDamageException`(B-4), `DAMAGED`/`LOST`이면 `BookCopyInvalidTransitionException`(B-1), `AVAILABLE`일 때만 `markAsDamaged()`. **두 불가 상태를 별도 코드로 나눈다** — 문서 `:487`이 그 이유를 적고 있다
- [ ] AC-C18: `repair` — `DAMAGED`가 아니면 `BookCopyNotDamagedException`(B-2), 맞으면 `repair()`
- [ ] AC-C19: `reportLost` — `DAMAGED`/`LOST`이면 `BookCopyInvalidTransitionException`, `AVAILABLE`/`LOANED`이면 `markAsLost()`. **`LOANED`에서도 허용**(→ B-4, 문서 `:536`)
- [ ] AC-C20: `reportLost`에 B-5 TODO 주석 — `// TODO: 대출 중 복본 분실 시 해당 대출도 LOST로 종료해야 한다 (→ B-5). Loan 도메인 구현 시 추가`
- [ ] AC-C21: 엔드포인트 4개 모두 `// TODO: LIBRARIAN 권한 검증 필요 (→ P-3) + 소속 지점 일치 검증 (→ P-8). JWT 도입 시 추가`

### Component `api-doc`
- [ ] AC-D1: `docs/03-api.md:439`의 "지점 내 유일"이 구현과 일치함을 확인 (Round 1 결정으로 이미 일치 — **수정 없음**)
- [ ] AC-D2: `:413`의 `totalCopies`, `:527`의 `closedLoan`이 구현과 일치함을 확인 (Round 2 결정으로 유지 — **수정 없음**)
- [ ] AC-D3: 다섯 엔드포인트의 에러 코드 표가 실제 예외 클래스와 1:1 대응함을 확인. 불일치가 나오면 **고치지 말고 보고**한다

### 전체
- [ ] AC-Z1: `./gradlew compileKotlin` → `BUILD SUCCESSFUL` (각 단계 끝에서)
- [ ] AC-Z2: `src/test` 변경 0건
- [ ] AC-Z3: `BookCopy.kt` 외 엔티티 변경 0건, `V2__fix_call_number_scope.sql` 외 마이그레이션 추가 0건

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "callNumber는 unique다" (범위 미지정) | 스키마·엔티티는 전역, 컬럼 주석·API 문서는 지점 내. 셋이 어긋남 | **지점 내 유일**로 확정. 스키마 버그로 판정하고 엔티티 인덱스 + V2 마이그레이션으로 교정 (Round 1) |
| "문서의 응답 스키마대로 만들면 된다" | `totalCopies`는 `BookCopyRepository` 없이, `closedLoan`은 Loan 도메인 없이 값을 만들 수 없음 | 둘 다 **유지, 고정값**(0 / null). 문서 수정 0건 (Round 2) |
| "엔티티를 만들어야 한다" | `Book`·`BookCopy`가 이미 있는가? | **이미 있다.** 상태 전이 메서드 5종(`loanOut`/`returned`/`markAsDamaged`/`markAsLost`/`repair`)도 전부 구현됨. 서비스·컨트롤러 계층만 비어 있음 |
| "상태 전이 실패를 엔티티가 처리한다" | 엔티티 `check`는 `IllegalStateException` → 500 | 엔티티는 그대로 두고 **서비스가 선검사**해 409를 던진다. `check`는 불변식 가드로 남는다 |
| "body 없는 엔드포인트도 Command를 만든다" | `UserService.approveLibrarian`은 bare UUID를 받음 | body가 없으면 **bare UUID**. Command 규칙은 body가 있는 경우에 적용 |
| "api-doc 컴포넌트에 할 일이 있다" | 라운드 1·2 결정이 모두 문서와 일치하는 방향으로 남 | **수정 0건.** 대조 검증만 수행하고, 불일치가 나오면 고치지 말고 보고 |

## Technical Context

**이미 존재 (건드리지 않음 — `BookCopy.kt` 인덱스 한 줄 제외)**

| 파일 | 내용 |
|---|---|
| `domains/book/entity/Book.kt` | `isbn`(val, unique idx), `title`/`author`/`publisher`/`publishedAt`/`category`(var), `description: String?`, `changeCategory`/`updateDescription` |
| `domains/library/entity/BookCopy.kt` | `callNumber`/`book`/`library`(val), `status`(protected set), `isLoanable`, 전이 메서드 5종, `BookCopyStatus` enum 4값 |
| `domains/book/service/CategoryService.kt` | `getCategoryEntity(categoryId): Category` — Book이 쓸 진입점 |
| `domains/library/service/LibraryService.kt` | `getLibraryEntity(libraryId): Library` — BookCopy가 쓸 진입점 |
| `common/exception/BusinessException.kt` | `code`/`status`/`rule`/`message` |
| `common/exception/GlobalExceptionHandler.kt` | `BusinessException` → 상태코드, `MethodArgumentNotValidException` → 400 `VALIDATION_FAILED`. **에러 처리 코드 추가 불필요** |
| `db/migration/V1__init_schema.sql` | `books`·`book_copies` 테이블 존재. `uk_books_isbn unique (isbn)`, `uk_copies_call_number unique (call_number)`(← 교정 대상) |

**따라야 할 패턴 — 파일로 지목**

| 패턴 | 참조 |
|---|---|
| Ref 타입 (service dto) | `domains/library/service/dto/LibraryRef.kt` |
| Ref 타입 (controller dto) | `domains/library/controller/dto/LibraryRefResponse.kt` |
| Result에 Ref 담기 | `domains/user/service/dto/UserResult.kt:28` (`user.library?.let { LibraryRef.from(it) }`) |
| 중복 검사 → 예외 | `domains/user/service/UserServiceImpl.kt:30-32` |
| 상태 선검사 → 도메인 예외 | `domains/user/service/UserServiceImpl.kt:66-67` |
| 서비스 인터페이스 KDoc | `domains/library/service/LibraryService.kt:12-15` |
| 트랜잭션 배치 | `domains/library/service/LibraryServiceImpl.kt:16,25` |
| 컨트롤러 한 줄 위임 | `domains/library/controller/LibraryController.kt:26-29` |
| 요청 DTO 검증 | `domains/library/controller/dto/CreateLibraryRequest.kt` |

**주의점**
- `BookCopyRepository.existsByLibraryIdAndCallNumber`는 파생 쿼리 프로퍼티 경로 `library.id`로 해석된다. `BookCopy.library`가 `@ManyToOne`이므로 동작한다.
- `open-in-view: false`. 컨트롤러는 Result/Response만 다루고 엔티티를 받지 않는다.
- `AuthController`와 달리 `BookController`/`BookCopyController`는 인증 컨텍스트가 없다 — `libraryId`를 **요청 body로 받는다**(문서 `:440`). JWT 도입 시 토큰에서 가져오도록 바뀐다.

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| `Book` | core domain (기존) | `isbn`, `title`, `author`, `publisher`, `publishedAt`, `description?`, `category` | `Book` N:1 `Category` |
| `BookCopy` | core domain (기존) | `callNumber`, `status`, `book`, `library` | `BookCopy` N:1 `Book`, N:1 `Library` |
| `CategoryRef` | supporting (신규) | `id`, `name` | `Category`의 얕은 참조. `BookResult`가 보유 |
| `BookRef` | supporting (신규) | `id`, `title` | `Book`의 얕은 참조. `BookCopyResult`가 보유 |
| `LibraryRef` | supporting (기존) | `id`, `name` | `BookCopyResult`가 재사용 |
| `BookResult` | supporting (신규) | `id`, `isbn`, `title`, `author`, `publisher`, `publishedAt`, `description?`, `category: CategoryRef`, `totalCopies` | ← `Book` |
| `BookCopyResult` | supporting (신규) | `id`, `callNumber`, `status`, `book: BookRef`, `library: LibraryRef` | ← `BookCopy` |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 7 | 7 | - | - | N/A |
| 2 | 7 | 0 | 0 | 7 | 100% |

2라운드 연속 동일 — 도메인 모델 수렴.

## Interview Transcript

<details>
<summary>Full Q&A (Round 0 + 2 rounds)</summary>

### Round 0 — 토폴로지 확인
**Q:** 사용자가 제시한 2개(`book`, `book-copy`) 외에, 탐색에서 드러난 `api-doc`(문서 정정)을 범위에 포함할까?
**A:** **포함** — 코드에 맞춰 문서 정정.
**Ambiguity:** 미측정

### Round 1 — book-copy / Constraint Clarity
**Q:** `callNumber` 유일성을 어느 범위로 확정할까? (스키마·엔티티=전역 vs 컬럼 주석·문서=지점 내)
**A:** **지점 내 유일 — 스키마 버그로 보고 수정.** 엔티티 인덱스 변경 + V2 마이그레이션 허용.
**Ambiguity:** 30.5% → 23.6% (Goal 0.92, Constraints 0.62, Criteria 0.58, Context 0.95)

### Round 2 — book / Constraint Clarity
**Q:** 문서에 있지만 이번 범위로 값을 만들 수 없는 응답 필드(`totalCopies`, `closedLoan`)를 어떻게 할까?
**A:** **둘 다 유지 — 0 / null 고정.** 문서 수정 0건.
**Ambiguity:** 23.6% → **13.2%** (Goal 0.93, Constraints 0.82, Criteria 0.78, Context 0.95)

**스코어링 정정:** Criteria를 라운드 1·2에서 0.58로 보고했으나 과소평가였다. "테스트 코드를 작성하지 않는다"가 초기 입력의 금지 사항에 이미 있어 검증 수단(`compileKotlin` + 문서 대조)은 처음부터 결정돼 있었다. 최종 0.78로 보정.

</details>

## Open Risks (임계치 통과 후 남은 것)

1. **`closedLoan: null`이 미구현을 가린다.** B-5가 없는 동안 LOANED 복본을 분실 처리하면 대출이 그대로 남는데, 응답의 `null`은 "닫을 대출이 없었다"로 읽힌다. Round 2에서 이 위험을 알고 선택했다. `AC-C20`의 TODO 주석이 유일한 흔적이므로, Loan 도메인 작업 시 이 주석을 반드시 찾아야 한다.
2. **`V2` 마이그레이션은 기존 데이터에 영향이 있다.** `ddl-auto: create-drop`이라 로컬에서는 무해하지만, 전역 유일 → 복합 유일은 **제약 완화** 방향이라 기존 행이 깨지지는 않는다.
3. **`libraryId`를 요청 body로 받는다.** JWT 도입 전까지 호출자가 임의의 지점을 지정할 수 있다. P-8 TODO가 이를 표시한다.

---

**Status: pending approval**
