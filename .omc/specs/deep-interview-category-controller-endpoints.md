# Deep Interview Spec: CategoryController 엔드포인트 3개 추가

## Metadata
- Interview ID: `di-category-controller-20261015`
- Rounds: 2 (+ Round 0 토폴로지)
- Final Ambiguity Score: **14.8%**
- Type: brownfield
- Generated: 2026-10-15
- Threshold: 0.2
- Threshold Source: `default`
- Initial Context Summarized: no
- Status: **PASSED**

## Clarity Breakdown

| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.92 | 0.35 | 0.322 |
| Constraint Clarity | 0.72 | 0.25 | 0.180 |
| Success Criteria | 0.85 | 0.25 | 0.213 |
| Context Clarity | 0.92 | 0.15 | 0.138 |
| **Total Clarity** | | | **0.852** |
| **Ambiguity** | | | **0.148** |

## Topology

| Component | Status | Description | Coverage / Deferral Note |
|-----------|--------|-------------|--------------------------|
| `controller` | active | `CategoryController` 엔드포인트 3개 + `controller/dto` 3개 | AC-1 ~ AC-9 |
| `api-doc` | active | `docs/03-api.md`를 실제 구현에 맞게 정정 | AC-10 ~ AC-12 |
| `category-result-extension` | **deferred** | `CategoryResult`에 `parent` / `path` 추가 | 사용자가 Round 0에서 명시적으로 제외. 서비스 계층을 열지 않기로 결정. 대신 문서를 코드에 맞춘다 (`api-doc`). 확인 시각: Round 0 |

## Goal

이미 완성된 `CategoryService`(`createCategory` / `updateCategory` / `getCategories`) 위에, 비어 있는 `CategoryController`를 채워 **세 엔드포인트를 노출한다**. 요청/응답 DTO는 `LibraryController`가 쓰는 패턴(`Request.toCommand()` / `Response.from(result)`)을 그대로 따른다. 그리고 현재 코드와 어긋나 있는 `docs/03-api.md`의 카테고리 절을 **구현 쪽에 맞춰 정정한다**.

```
POST /api/v1/categories        → 201 Created
PUT  /api/v1/categories/{id}   → 200 OK
GET  /api/v1/categories        → 200 OK (트리)
```

## Constraints

- **서비스 계층을 수정하지 않는다.** `CategoryService` / `CategoryServiceImpl` / `service/dto/*` 는 그대로 둔다. 컨트롤러는 기존 시그니처에 맞춘다.
- **`PUT`은 전체 교체 의미다.** `parentId`를 생략하면 `changeParent(null)`이 실행되어 **해당 분류가 최상위로 이동한다** (Round 1 확정). 이 동작을 바꾸지 않고, 문서에 명시한다.
- **에러 처리 코드를 추가하지 않는다.** `GlobalExceptionHandler`가 `BusinessException`을 상태코드별로 이미 처리하고 있어 404(`CATEGORY_NOT_FOUND`) · 409(`CATEGORY_CIRCULAR_REFERENCE`) · 400(`VALIDATION_FAILED`)이 자동으로 나간다.
- **권한 검증을 구현하지 않는다.** JWT가 아직 없으므로 `LibraryController` / `UserController`와 동일하게 `// TODO: ADMIN 권한 검증 필요` 주석만 남긴다.
- **응답에 `children`을 포함한다.** `CategoryResult`가 주는 형태(`id`, `name`, `children`)를 그대로 쓴다. `parent` / `path`는 제공하지 않는다.
- 응답 DTO는 `CategoryResult`와 같은 재귀 구조여야 한다 (`children: List<CategoryResponse>`).
- 테스트 파일을 추가하지 않는다 (Round 2 확정).

## Non-Goals

- `CategoryResult`에 `parent` / `path` 추가 — Round 0에서 명시적으로 제외
- `@WebMvcTest` / `@SpringBootTest` 테스트 작성 — Round 2에서 명시적으로 제외
- 실제 권한 검증 (`@PreAuthorize` 등) 구현
- `GET /api/v1/categories`의 페이징 / 필터 / 정렬
- `CategoryServiceImpl.getCategories()`의 N+1 개선 (`@EntityGraph`가 1단계만 즉시 로딩하므로 깊이 2 이상에서 추가 쿼리 발생 — 이번 범위 밖)
- 삭제 엔드포인트 (`DELETE`)

## Acceptance Criteria

### `controller`
- [ ] AC-1: `CategoryController`가 `CategoryService`를 생성자 주입으로 받는다.
- [ ] AC-2: `POST /api/v1/categories` — `@ResponseStatus(HttpStatus.CREATED)`, `@RequestBody @Valid CreateCategoryRequest`를 받아 `CategoryResponse`를 반환한다.
- [ ] AC-3: `PUT /api/v1/categories/{id}` — `@PathVariable id: UUID`와 `@RequestBody @Valid UpdateCategoryRequest`를 받아 `CategoryResponse`를 반환한다 (기본 200).
- [ ] AC-4: `GET /api/v1/categories` — `List<CategoryResponse>`를 반환한다.
- [ ] AC-5: `CreateCategoryRequest(name: String, parentId: UUID?)`에 `toCommand(): CreateCategoryCommand`가 있다.
- [ ] AC-6: `UpdateCategoryRequest(name: String, parentId: UUID?)`에 `toCommand(categoryId: UUID): UpdateCategoryCommand`가 있다 — `LibraryController`의 `UpdateLibraryNameRequest.toCommand(libraryId)` 패턴과 동일.
- [ ] AC-7: `CategoryResponse(id: UUID, name: String, children: List<CategoryResponse>)`에 `companion object { fun from(result: CategoryResult): CategoryResponse }`가 있고 **재귀적으로** `children`을 변환한다.
- [ ] AC-8: `name`에 `@field:NotBlank` + `@field:Size(max = 50)`가 붙어 있다.
- [ ] AC-9: 세 엔드포인트에 `// TODO: ADMIN 권한 검증 필요` / `// TODO: 인증 필요` 주석이 붙어 있다 (POST·PUT은 ADMIN → P-6, GET은 인증 필요).

### `api-doc`
- [ ] AC-10: `docs/03-api.md:190` 표 행이 `PATCH` → `PUT`으로 바뀐다.
- [ ] AC-11: `docs/03-api.md:835` 절 제목과 요청 설명이 `PUT` + **전체 교체**로 바뀌고, `name`이 필수(`string`)로 정정되며, **`parentId` 생략 시 최상위로 이동한다**는 동작이 명시된다.
- [ ] AC-12: `docs/03-api.md`의 POST(:824) · PUT 응답 예시가 `{ id, name, children }` 형태로 정정된다 (`parent` / `path` 제거).

### 전체
- [ ] AC-13: `./gradlew compileKotlin` → `BUILD SUCCESSFUL`
- [ ] AC-14: `src/main` 파일 수가 기존 + 3 (신규 DTO 3개), `src/test` 변경 0건

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "엔드포인트만 추가하면 된다" | 서비스 계층이 이미 있는가? | **있다.** `CategoryService` 3개 메서드 완비. 컨트롤러만 비어 있음 (`CategoryController.kt:8`) |
| "`PUT`이 맞다" | `docs/03-api.md:835`는 `PATCH` + 부분 수정으로 규정 | `UpdateCategoryCommand.name`이 non-null이고 `updateCategory()`가 무조건 `changeName`+`changeParent`를 호출 → **서비스는 이미 PUT 모양**. 코드를 따르고 문서를 정정 |
| "`parentId`를 안 보내면 상위가 유지된다" | `CategoryServiceImpl.kt:38`은 조건 없이 `changeParent(newParent)` 호출 | **유지되지 않는다. 최상위로 이동한다.** Round 1에서 현재 동작을 유지하기로 확정하고 문서에 명시 |
| "응답은 문서대로 `parent` + `path`" | `CategoryResult`에는 둘 다 없음 | `CategoryResult`를 건드리지 않기로 결정 → 문서를 `children` 형태로 정정 |
| "에러 핸들링을 컨트롤러에 넣어야 한다" | `GlobalExceptionHandler`가 이미 있는가? | **있다.** `BusinessException` → 상태코드 자동 매핑. 404/409/400 전부 커버. 추가 코드 0줄 |
| "검증을 위해 테스트가 필요하다" | 프로젝트에 테스트가 있는가? | 컨텍스트 로드 1건뿐. Round 2에서 **컴파일만**으로 확정 |

## Technical Context

**이미 존재하는 것 (건드리지 않음)**

| 파일 | 상태 |
|---|---|
| `domains/book/service/CategoryService.kt` | 3개 메서드 + `getCategoryEntity` 완비 |
| `domains/book/service/CategoryServiceImpl.kt` | C-2 순환 검사 포함, `@Transactional` 적용 |
| `domains/book/service/dto/{CategoryResult, CreateCategoryCommand, UpdateCategoryCommand}.kt` | 완비 |
| `domains/book/repository/CategoryRepository.kt` | `findAllByParentIsNull()` + `@EntityGraph(["_children"])` |
| `common/exception/CategoryExceptions.kt` | 404 / 409 `BusinessException` 2종 |
| `common/exception/GlobalExceptionHandler.kt` | `BusinessException` + `MethodArgumentNotValidException` 처리 |

**참고할 패턴** — `domains/library/controller/LibraryController.kt` 및 그 `controller/dto/`

- 컨트롤러: 생성자 주입, 식 본문(`=`), `Response.from(service.xxx(request.toCommand()))` 한 줄
- 요청 DTO: `data class` + `@field:` 검증 + `toCommand()`
- 응답 DTO: `data class` + `companion object { fun from(result) }`
- 권한: `// TODO: ADMIN 권한 검증 필요` 주석

**주의점**

- `CategoryResult.from()`은 `category.children`을 재귀 순회한다. `getCategories()`의 `@Transactional(readOnly = true)` 안에서 호출되므로 동작하지만, `@EntityGraph`가 1단계만 즉시 로딩하므로 **깊이 2 이상에서 추가 쿼리가 발생한다.** 이번 범위 밖이되 알고 있어야 한다.
- `open-in-view: false`이므로 컨트롤러에서 엔티티의 지연 필드를 만지면 `LazyInitializationException`이 난다. 컨트롤러는 `CategoryResult`만 다루므로 안전하다.
- `Category.name`은 `@Column(length = 50)`.

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| `Category` | core domain | `id`, `name`, `parent`, `children` | 자기참조 계층 (`parent` N:1, `children` 1:N) |
| `CreateCategoryRequest` | supporting (신규) | `name`, `parentId?` | → `CreateCategoryCommand` |
| `UpdateCategoryRequest` | supporting (신규) | `name`, `parentId?` | → `UpdateCategoryCommand` (+ path `id`) |
| `CategoryResponse` | supporting (신규) | `id`, `name`, `children` | ← `CategoryResult`, 자기 재귀 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 4 | 4 | - | - | N/A |
| 2 | 4 | 0 | 0 | 4 | 100% |

2라운드 연속 동일 — 도메인 모델 수렴.

## Interview Transcript

<details>
<summary>Full Q&A (Round 0 + 2 rounds)</summary>

### Round 0 — 토폴로지 확인
**Q:** 최대 3개 산출물(컨트롤러+DTO / 문서 정정 / `CategoryResult` 확장) 중 어디까지가 범위인가?
**A:** **1 + 2 (컨트롤러 + 문서 정정)**. `CategoryResult` 확장은 제외.
**Ambiguity:** 미측정

### Round 1 — Constraint Clarity
**Q:** `PUT` 요청에서 `parentId`를 보내지 않으면 어떻게 동작해야 하나?
**A:** **최상위로 이동 (현재 동작 유지).** 서비스 수정 없이 컨트롤러만 추가.
**Ambiguity:** 30.4% → 22.2% (Goal 0.90, Constraints 0.72, Criteria 0.58, Context 0.92)

### Round 2 — Success Criteria
**Q:** 완료 기준을 무엇으로 잡을까?
**A:** **컴파일만** (프로젝트 현재 관행 유지). 테스트 파일 추가 없음.
**Ambiguity:** 22.2% → **14.8%** (Goal 0.92, Constraints 0.72, Criteria 0.85, Context 0.92)

</details>

## Open Assumption (임계치 통과 후 남은 것)

- **`name` 검증 범위**: `@field:NotBlank` + `@field:Size(max = 50)`으로 간다. 근거는 `Category.name`의 `@Column(length = 50)`. `LibraryController`는 `@Size(min = 2, max = 20)`을 쓰지만 그것은 지점명 기준이고, 분류명은 "시"처럼 한 글자가 나올 수 있어 `min`을 걸지 않는다. **되돌리기 쉬움** — 어노테이션 한 줄.

---

**Status: pending approval**
