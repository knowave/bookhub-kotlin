# Deep Interview Spec: springdoc-openapi (Swagger UI) 도입

## Metadata
- Interview ID: `di-springdoc-20261020`
- Rounds: 2 (+ Round 0 토폴로지)
- Final Ambiguity Score: **7.7%**
- Type: brownfield
- Generated: 2026-10-20
- Threshold: 0.2
- Threshold Source: `default`
- Initial Context Summarized: no
- Status: **PASSED**

## Clarity Breakdown

| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.95 | 0.35 | 0.333 |
| Constraint Clarity | 0.93 | 0.25 | 0.233 |
| Success Criteria | 0.88 | 0.25 | 0.220 |
| Context Clarity | 0.92 | 0.15 | 0.138 |
| **Total Clarity** | | | **0.923** |
| **Ambiguity** | | | **0.077** |

## Topology

| Component | Status | Description | Coverage / Deferral Note |
|-----------|--------|-------------|--------------------------|
| `dependency` | active | `springdoc-openapi-starter-webmvc-ui` 추가 + Boot 4.1 호환 확인 | AC-1 ~ AC-2 |
| `security-path` | active | `SecurityConfig`에 Swagger 경로 `permitAll` | AC-3 ~ AC-4 |
| `openapi-config` | active | `OpenApiConfig` 빈 — 제목 + 전역 bearer 스킴 | AC-5 ~ AC-8 |
| `yaml-config` | active | `application.yaml`의 `springdoc` 블록 | AC-9 |
| `schema-annotations` | active | DTO 23개 80필드에 `@field:Schema` | AC-10 ~ AC-13 |

Round 0에서는 4개였으나 Round 1에서 `openapi-config`가 분리돼 5개가 됐다 — bearer 스킴은 yaml로 정의할 수 없다.

## Goal

NestJS Swagger처럼 **브라우저에서 클릭으로 18개 엔드포인트를 전부 호출**할 수 있게 만든다. 현재는 `curl`로 토큰을 만들고 헤더를 붙여야 해 시간이 많이 든다.

핵심은 **Authorize 버튼**이다. 18개 중 16개가 Bearer 토큰을 요구하므로, 토큰을 한 번 넣으면 이후 모든 요청에 자동으로 붙어야 목적이 달성된다.

## 확인된 사실 (인터뷰 중 조사)

### "application.yaml 설정만 하면 열린다" — 아니다, 순서가 반대다
**의존성을 추가해야** 열린다. 대신 의존성만 넣으면 **yaml 없이도** `/swagger-ui.html`과 `/v3/api-docs`가 기본 동작한다. yaml은 커스터마이징 전용이다.

### `@ApiProperty` / `@ApiOperation` — 대응물은 있으나 이름이 다르다
Spring 자체 제공이 아니라 **swagger-core**(`io.swagger.v3.oas.annotations.*`)에서 오며, springdoc이 전이 의존성으로 끌어온다.

| NestJS | Spring (OpenAPI 3) | 비고 |
|---|---|---|
| `@ApiProperty()` | `@Schema` | Kotlin에선 **`@field:Schema`** (타깃 지정 필수) |
| `@ApiOperation()` | **`@Operation`** | `@ApiOperation`은 Swagger 2(springfox) 시절 이름 |
| `@ApiTags()` | `@Tag` | |
| `@ApiResponse()` | `@ApiResponse` | 이름 동일 |
| `@ApiBearerAuth()` | `@SecurityRequirement` | |

**`@ApiOperation`·`@ApiProperty`는 현재 쓰이지 않는 이름이다.** 그 이름으로 검색하면 springfox 자료가 나오는데 springfox는 Spring Boot 3+ 에서 동작하지 않는다.

### 버전
Spring Boot **4.1.1** / Kotlin 2.3.21 / JDK 21. springdoc **3.x**가 Boot 4.x 라인이고 **3.1.1**이 권장 안정판이다. 공식 호환 매트릭스는 **Boot 4.0.x까지만 명시**하며 4.1은 없다 — 기동으로 확인해야 한다.

출처: [springdoc.org v4 FAQ](https://springdoc.org/v4/) · [springdoc-openapi GitHub](https://github.com/springdoc/springdoc-openapi) · [start.spring.io #2088](https://github.com/spring-io/start.spring.io/issues/2088)

## Constraints

### `SecurityConfig`를 반드시 건드린다
현재 `anyRequest().authenticated()`이므로 Swagger 경로가 401로 막힌다. 의존성만 넣으면 **UI가 열리지 않는다.** 이전 세션에서 보안 설정은 손대지 말라는 지침이 있었으나, 이 작업은 그 파일 수정이 전제다 (Round 0에서 승인).

허용할 경로는 셋뿐이다 — `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`. 기존 `permitAll` 두 줄과 `anyRequest().authenticated()`는 그대로 둔다.

### bearer 스킴은 전역으로 (Round 1 확정)
`OpenApiConfig` 빈에서 `addSecurityItem`으로 전역 적용한다. 컨트롤러에 `@SecurityRequirement`를 16번 다는 방식은 쓰지 않는다 — `SecurityConfig`와 이중 관리가 되어 조용히 어긋난다.

부작용: `permitAll`인 2개 엔드포인트에도 자물쇠 아이콘이 뜬다. **표시만 부정확하고 토큰 없이도 호출된다.** 감수한다.

### 애노테이션은 `@Schema`만 (사용자 지시)
`@Tag` / `@Operation` / `@ApiResponse`를 **붙이지 않는다.** 컨트롤러 6개는 손대지 않는다.

### `@field:Schema`는 모든 필드에 description + example (Round 2 확정)
DTO 23개 **80필드 전부**. `example`이 핵심이다 — UUID나 ISBN을 매번 손으로 만들지 않아도 "Try it out"이 그럴듯한 본문으로 채워진다.

springdoc이 이미 자동으로 읽는 것은 **다시 적지 않는다**: 타입, Kotlin nullability 기반 `required`, `@field:NotBlank`/`@field:Size`/`@field:Email`/`@field:Pattern` 제약. `@Schema(required = ...)`나 `@Schema(maxLength = ...)`를 중복으로 쓰지 않는다.

### 그 밖
- 의존성 버전을 **명시**한다. springdoc은 Spring Boot BOM 관리 대상이 아니다.
- 테스트 코드를 작성하지 않는다.
- 엔티티·서비스·리포지토리를 수정하지 않는다.
- `docs/03-api.md`를 수정하지 않는다.

## Non-Goals

- 컨트롤러 애노테이션 (`@Tag`, `@Operation`, `@ApiResponse`) — 사용자가 명시적으로 제외
- `docs/03-api.md`를 OpenAPI 스펙으로 대체하거나 동기화
- OpenAPI 스펙 파일을 빌드 산출물로 내보내기
- 운영 환경에서 Swagger UI 비활성화 설정 (프로파일 분기)
- API 버저닝 / 그룹 분리 (`GroupedOpenApi`)
- **`SignUpMemberRequest`의 버그 수정** — 아래 Open Risks 참조. 별도 작업
- 엔티티·서비스·리포지토리 수정

## Acceptance Criteria

### `dependency`
- [ ] AC-1: `build.gradle.kts`에 `implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")` 추가. 기존 의존성 블록의 주석 구획(`// Spring` 등)을 따른다
- [ ] AC-2: `./gradlew compileKotlin` 통과 + **앱이 실제로 기동**한다. 기동 실패 시 3.1.1이 Boot 4.1.1과 맞지 않는 것이므로 **버전을 바꾸지 말고 멈추고 보고**한다

### `security-path`
- [ ] AC-3: `SecurityConfig`의 `authorizeHttpRequests` 블록에 세 경로를 `permitAll`로 추가 — `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`
- [ ] AC-4: 기존 두 `permitAll` 줄과 `anyRequest().authenticated()`, 필터 등록, `csrf`/`formLogin`/`httpBasic`/`sessionManagement` 설정은 **한 글자도 바꾸지 않는다**

### `openapi-config`
- [ ] AC-5: `common/config/OpenApiConfig.kt` 신규. `@Configuration` + `@Bean fun openAPI(): OpenAPI`. 기존 `JpaConfig`/`PasswordConfig`와 같은 패키지·형식
- [ ] AC-6: `Info`에 제목·설명·버전. 제목은 `"BookHub API"`
- [ ] AC-7: `bearerAuth` 보안 스킴 — `SecurityScheme().type(HTTP).scheme("bearer").bearerFormat("JWT")`
- [ ] AC-8: `addSecurityItem(SecurityRequirement().addList("bearerAuth"))`로 **전역 적용**. 컨트롤러에 `@SecurityRequirement`를 달지 않는다

### `yaml-config`
- [ ] AC-9: `application.yaml` 최상위에 `springdoc` 블록. `swagger-ui.path`(접속 경로)와 `swagger-ui.operations-sorter`/`tags-sorter` 정도. `jwt:`/`logging:`과 같은 깊이. 기존 블록은 건드리지 않는다

### `schema-annotations`
- [ ] AC-10: `controller/dto` 아래 **23개 파일 전부**의 **80개 필드 전부**에 `@field:Schema(description = ..., example = ...)`
- [ ] AC-11: 어노테이션 타깃은 반드시 **`@field:`**. `@Schema`만 쓰면 Kotlin 생성자 파라미터에 붙어 스키마에 반영되지 않는다 (`@field:NotBlank`와 같은 이유)
- [ ] AC-12: `required`·`maxLength`·`pattern` 등 **springdoc이 기존 `jakarta.validation` 애노테이션에서 이미 추론하는 속성을 `@Schema`에 중복 기재하지 않는다**
- [ ] AC-13: `example` 값은 `docs/03-api.md`의 예시와 일치시킨다 (ISBN `9788983920683`, 청구기호 `833.6-무라카-3` 등). UUID는 `docs`의 축약형(`c1...`)이 아니라 **유효한 UUID 문자열**을 쓴다 — UI에서 그대로 전송되어야 하므로

### 전체
- [ ] AC-Z1: `./gradlew compileKotlin compileTestKotlin` → `BUILD SUCCESSFUL`
- [ ] AC-Z2: 기존 테스트 19건 전부 통과
- [ ] AC-Z3: **앱을 기동해 `/v3/api-docs`가 200과 JSON을 반환**하고, 그 JSON에 18개 경로와 `securitySchemes.bearerAuth`가 들어 있음을 확인
- [ ] AC-Z4: 컨트롤러 6개 변경 0건, 엔티티·서비스·리포지토리 변경 0건, `docs/` 변경 0건

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "application.yaml 설정만 하면 열린다" | 의존성 없이 되는가? | **아니다.** 의존성이 먼저고, 그다음엔 yaml 없이도 열린다. 순서가 반대 |
| "`@ApiProperty`/`@ApiOperation`이 있는가" | 그 이름 그대로인가? | **대응물은 있으나 이름이 다르다** — `@Schema`/`@Operation`. 그 이름은 springfox 시절 것이고 springfox는 Boot 3+ 미지원 |
| "의존성만 넣으면 UI가 열린다" | `SecurityConfig`는? | **401로 막힌다.** `anyRequest().authenticated()`. 경로 3개 `permitAll` 필수 |
| "Swagger UI를 열면 API를 테스트할 수 있다" | 16개가 토큰을 요구하는데 UI가 헤더를 넣을 수 있는가? | **Authorize 버튼이 없으면 못 넣는다.** `OpenApiConfig` 빈으로 전역 bearer 스킴 추가 (Round 1) |
| "bearer 스킴도 yaml로 된다" | springdoc에 해당 프로퍼티가 있는가? | **없다.** Kotlin 설정 클래스가 필요하다 — Round 0에 없던 5번째 컴포넌트 |
| "모든 필드에 @Schema를 달아야 제약이 보인다" | springdoc이 validation을 읽는가? | **읽는다.** `@field:Size(max=50)`가 `maxLength`로 자동 반영. `@Schema`가 더하는 건 설명과 예시뿐 (Round 2) |

## Technical Context

**현재 API 표면**

| 컨트롤러 | 엔드포인트 | 인증 |
|---|---|---|
| `AuthController` | 3 | **permitAll** (`/api/v1/auth/**`) |
| `LibraryController` | 4 | GET 목록만 permitAll, 나머지 authenticated |
| `BookController` | 1 | authenticated |
| `CategoryController` | 3 | authenticated |
| `BookCopyController` | 4 | authenticated |
| `UserController` | 3 | authenticated |
| **합계** | **18** | 16개가 Bearer 토큰 필요 |

**DTO 23파일 / 80필드** — `auth` 5, `book` 7, `library` 9, `user` 2

**수정 대상 (5파일 + 신규 1)**

| 파일 | 변경 |
|---|---|
| `build.gradle.kts` | 의존성 1줄 |
| `common/config/SecurityConfig.kt` | `permitAll` 3줄 |
| `common/config/OpenApiConfig.kt` | **신규** |
| `src/main/resources/application.yaml` | `springdoc` 블록 |
| `controller/dto/*.kt` × 23 | `@field:Schema` × 80 |

**주의점**
- Spring Boot 4에서 web starter 이름이 `spring-boot-starter-webmvc`로 바뀌었다 (`build.gradle.kts:26`). springdoc 아티팩트명 `springdoc-openapi-starter-webmvc-ui`는 springdoc 자체 명명이며 무관하다.
- `io.spring.dependency-management` 플러그인이 적용돼 있지만 springdoc은 Spring Boot BOM 관리 대상이 아니므로 **버전 명시가 필수**다.
- `SignUpResponse`가 서비스 계층 DTO인 `LibraryRef`를 그대로 응답에 노출한다. 같은 파일의 `SignUpLibraryResponse`는 **사용처 0건**이다. 이번 범위 밖이지만 `@Schema`를 붙일 때 눈에 띌 것이다.

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| `OpenAPI 문서` | 산출물 | `info`, `paths`, `components`, `security` | springdoc이 컨트롤러·DTO에서 생성 |
| `SecurityScheme` | supporting (신규) | `bearerAuth`, HTTP/bearer/JWT | `OpenApiConfig`가 정의, 전역 적용 |
| `Swagger UI` | 외부 표면 | `/swagger-ui.html` | `OpenAPI 문서`를 렌더 |
| `Request/Response DTO` | 기존 (23개) | 80 필드 | `@field:Schema`가 설명·예시를 붙임 |
| `SecurityConfig` | 기존 | 경로별 인가 규칙 | Swagger 경로를 열어줘야 함 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 5 | 5 | - | - | N/A |
| 2 | 5 | 0 | 0 | 5 | 100% |

## Interview Transcript

<details>
<summary>Full Q&A (Round 0 + 2 rounds)</summary>

### Round 0 — 토폴로지 확인
**Q:** springdoc 도입 / SecurityConfig / yaml / 애노테이션 중 어디까지?
**A:** **전부.** (이후 "애노테이션은 `@Schema`만"으로 축소)
**Ambiguity:** 미측정

### Round 1 — Constraint Clarity
**Q:** 16개 엔드포인트가 Bearer를 요구한다. Swagger UI에서 JWT를 어떻게 넣을까?
**A:** **`OpenApiConfig` 빈 + 전역 bearer 스킴.**
**Ambiguity:** 29.0% → 16.9% (Goal 0.93, Constraints 0.85, Criteria 0.85, Context 0.92)

### 턴 중 추가 지시
**사용자:** "spring docs 애노테이션은 schema만 붙여줘."
**결과:** 컨트롤러 애노테이션 제외. 수정 파일 29 → 23.

### Round 2 — Constraint Clarity
**Q:** `@field:Schema`를 어느 깊이로? springdoc이 validation을 이미 읽는다.
**A:** **모든 필드에 description + example.**
**Ambiguity:** 16.9% → **7.7%** (Goal 0.95, Constraints 0.93, Criteria 0.88, Context 0.92)

</details>

## Open Risks

1. **⚠️ 범위 밖에서 발견한 버그 — `SignUpMemberRequest.kt:28`**
   ```kotlin
   fun toCommand() = SignUpMemberCommand(
       email = email,
       password = password,
       name = password,     // ← name = name 이어야 한다
   )
   ```
   **일반 회원 가입 시 비밀번호가 이름으로 저장된다.** 평문 비밀번호가 `users.name` 컬럼에 들어간다. `SignUpLibrarianRequest`는 `name = name`으로 올바르다. 이번 작업 범위가 아니라 고치지 않았다. **별도로 처리할 것.**

2. **Boot 4.1 + springdoc 3.1.1 호환성이 공식 문서에 없다.** 매트릭스는 4.0.x까지만 명시한다. AC-2가 기동 확인을 요구하는 이유이며, 실패하면 버전을 바꿔가며 시도하지 말고 멈추고 보고한다.

3. **전역 bearer 스킴 때문에 `permitAll` 2개에도 자물쇠가 뜬다.** 표시만 부정확하고 동작은 정상이다. 거슬리면 그 4개 메서드(`AuthController` 3 + `LibraryController` GET 1)에 `@SecurityRequirements`(빈 배열)를 달면 된다 — Round 1의 3번 선택지였다.

4. **Swagger UI가 운영에서도 열린다.** 프로파일 분기를 범위에서 뺐으므로, 배포한다면 `springdoc.api-docs.enabled: false`를 운영 프로파일에 넣어야 한다.

---

**Status: pending approval**
