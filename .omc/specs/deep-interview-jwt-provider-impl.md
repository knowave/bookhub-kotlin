# Deep Interview Spec: JwtProviderImpl

## Metadata
- Interview ID: `di-jwt-provider-20261019`
- Rounds: 2 (+ Round 0 토폴로지)
- Final Ambiguity Score: **7.4%**
- Type: brownfield
- Generated: 2026-10-19
- Threshold: 0.2
- Threshold Source: `default`
- Initial Context Summarized: no
- Status: **PASSED**

## Clarity Breakdown

| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.94 | 0.35 | 0.329 |
| Constraint Clarity | 0.90 | 0.25 | 0.225 |
| Success Criteria | 0.92 | 0.25 | 0.230 |
| Context Clarity | 0.95 | 0.15 | 0.143 |
| **Total Clarity** | | | **0.926** |
| **Ambiguity** | | | **0.074** |

## Topology

| Component | Status | Description | Coverage / Deferral Note |
|-----------|--------|-------------|--------------------------|
| `jwt-provider` | active | `JwtProviderImpl` + 토큰 예외 + 단위 테스트 | AC-1 ~ AC-18 |
| `jwt-interface` | active | `JwtProvider`에 `parseRefresh` 추가 | AC-19. **금지 해제됨** (Round 1) |
| `app-config` | active | `application.yaml`의 `spring.jwt.*` → 최상위 `jwt.*` 재배치 | AC-20 ~ AC-21 |
| `env-key` | active | `.env`의 `JWT_SECURITY` → `JWT_SECRET` 개명 | AC-22. **금지 해제됨** (Round 0) |

## Goal

`common/security/JwtProviderImpl.kt`를 작성해 이미 정의된 `JwtProvider`를 구현한다. HS256으로 access/refresh 토큰을 발급하고, `type` 클레임으로 둘을 구분해 **서로 바꿔 쓸 수 없게** 강제한다. 설정값은 최상위 `jwt.*`에서 `@Value`로 주입하고, 시크릿이 없거나 짧으면 **부팅을 실패시킨다.**

## Constraints

### 설정 — 기존 블록을 옮긴다, 새로 추가가 아니다
`application.yaml:36-38`에 이미 아래가 있다. **이것을 제거하고** 최상위로 옮긴다.
```yaml
spring:
  jwt:                      # ← 제거. spring.*은 스프링이 소유하는 네임스페이스다
    secret: ${JWT_SECURITY}
    access-token-validity: 3600
```
→
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-validity-seconds: 3600
  refresh-token-validity-seconds: 1209600
```
기본값을 두지 않는다. `.env`는 `spring.config.import: optional:file:.env[.properties]`로 이미 로딩되며(dotenv 의존성 불필요), `optional:`은 `.env` 파일 부재만 허용할 뿐 **플레이스홀더 미해결은 부팅을 실패시킨다** — 요구사항대로다.

### 환경변수 개명 (Round 0 확정)
`.env`의 `JWT_SECURITY`를 `JWT_SECRET`으로 바꾼다. `.env`는 `.gitignore:41`에 있어 추적되지 않으므로 **다른 머신·CI의 `.env`도 같은 개명이 필요하다.**

### 토큰 타입 강제 (Round 1 확정)
`type` 클레임(`access` / `refresh`)을 넣고, 파싱 쪽에서 **기대 타입과 다르면 거부한다.** 인터페이스에 `parseRefresh`를 추가해 호출자가 타입을 지정하게 한다. 이것이 없으면 14일짜리 refresh 토큰이 1시간짜리 access 자리에 그대로 통과한다.

### `libraryId` null 처리 — 한 가지 규칙
`MEMBER`/`ADMIN`은 `libraryId`가 null이다(M-6). **"클레임 부재 == null"** 하나로 통일한다. jjwt는 `claim(name, null)`에 대해 클레임을 넣지 않으므로, 쓰기는 `principal.libraryId?.toString()`을 그대로 넘기고 읽기는 부재·null을 똑같이 null로 받는다. 별도 분기를 만들지 않는다.

### 키 — 한 번 만들어 재사용
`init` 블록에서 길이를 검증하고 `SecretKey`를 한 번 만들어 필드에 보관한다. 매 호출 생성 금지. HS256은 256비트(32바이트) 이상을 요구하며 `Keys.hmacShaKeyFor`가 미달 시 `WeakKeyException`을 던지므로, **그 전에** 직접 검사해 읽을 수 있는 메시지를 남긴다.

### 만료와 서명 오류를 구분하지 않는다 — 판단 근거
둘 다 `AUTH_INVALID_TOKEN` 401 하나로 응답한다. 이유 셋:
1. 지시가 코드 하나(`AUTH_INVALID_TOKEN`)만 정했다. 구분하려면 두 번째 코드가 필요하고 그건 별도 결정이다.
2. 재발급 엔드포인트가 아직 없어 **클라이언트가 "만료"를 알아도 할 수 있는 일이 다르지 않다.**
3. 구분 응답은 공격자에게 "서명은 맞았다"를 알려주는 오라클이 된다. 이득이 생기기 전까지는 안 여는 쪽이 싸다.

`BusinessException`에 `cause` 파라미터가 없어 원인 예외를 실어 보내지도 않는다. 구분이 필요해지는 시점은 refresh 엔드포인트가 생길 때다.

### 그 밖
- `@Component`로 등록한다 (동작 코드).
- 예외 메시지에 **토큰 값이나 시크릿을 넣지 않는다.**
- `UserPrincipal`을 수정하지 않는다.
- `validate`는 예외를 밖으로 던지지 않고 `Boolean`만 반환한다. `parse`는 실패 시 예외를 던진다. 하나로 합치지 않는다.

## Non-Goals

- 인증 필터 / `SecurityConfig` 변경 — `JwtProvider` 사용처는 현재 **0건**이고 이번에도 만들지 않는다
- 재발급(refresh) 엔드포인트, 로그인 엔드포인트
- 토큰 블랙리스트 / 폐기(revocation)
- 만료와 서명 오류를 다른 코드로 구분
- `UserPrincipal` 수정
- `BusinessException`에 `cause` 추가

## Acceptance Criteria

### Component `jwt-interface`
- [ ] AC-19: `common/security/JwtProvider.kt`에 `fun parseRefresh(token: String): UserPrincipal`을 **추가한다.** 기존 네 메서드의 시그니처는 바꾸지 않는다. `parse`의 KDoc에 "access 토큰만 받는다", `parseRefresh`에 "refresh 토큰만 받는다"를 적는다

### Component `jwt-provider`
- [ ] AC-1: `common/security/JwtProviderImpl.kt` — `@Component`, `JwtProvider` 구현
- [ ] AC-2: 생성자에서 `@Value("\${jwt.secret}") secret: String`, `@Value("\${jwt.access-token-validity-seconds}") accessValiditySeconds: Long`, `@Value("\${jwt.refresh-token-validity-seconds}") refreshValiditySeconds: Long`를 받는다
- [ ] AC-3: `init` 또는 프로퍼티 초기화에서 `secret.toByteArray(Charsets.UTF_8).size >= 32`를 검사한다. 미달이면 예외를 던져 **부팅을 실패시킨다.** 메시지는 필요한 바이트 수와 실제 바이트 수만 담고 **시크릿 값을 넣지 않는다**
- [ ] AC-4: `SecretKey`는 `Keys.hmacShaKeyFor(...)`로 **한 번만** 만들어 `private val`에 보관한다. 메서드 안에서 생성하지 않는다
- [ ] AC-5: 토큰 클레임 네 개 — `sub`(id, UUID 문자열), `role`(`UserRole` 이름), `libraryId`(UUID 문자열 또는 부재), `type`(`access`/`refresh`). 추가로 `iat`·`exp`를 넣는다
- [ ] AC-6: `type` 클레임 값은 상수로 둔다. 문자열 리터럴을 생성·파싱 양쪽에 따로 쓰지 않는다 (오타가 컴파일로 안 잡히는 지점)
- [ ] AC-7: 클레임 **키 이름도 상수**로 둔다 (`role`, `libraryId`, `type`). 같은 이유
- [ ] AC-8: `createAccessToken` / `createRefreshToken`은 만료 시간만 다르고 나머지 조립 로직을 공유한다 (private 헬퍼 하나)
- [ ] AC-9: 만료는 `Instant.now().plusSeconds(validitySeconds)`로 계산한다. 밀리초 곱셈을 직접 하지 않는다
- [ ] AC-10: `validate(token)` — 서명과 만료만 확인하고 `Boolean` 반환. **어떤 예외도 밖으로 나가지 않는다** (`runCatching` 또는 try/catch로 전부 흡수)
- [ ] AC-11: `parse(token)` — `type == access`를 강제하고 `UserPrincipal`로 조립. 실패 시 예외
- [ ] AC-12: `parseRefresh(token)` — `type == refresh`를 강제. 그 외 `parse`와 동일
- [ ] AC-13: `parse`/`parseRefresh`의 공통 로직을 private 헬퍼로 묶고 기대 타입만 파라미터로 받는다
- [ ] AC-14: `libraryId`는 부재와 null을 **똑같이** null로 처리한다. `MEMBER`/`ADMIN` 토큰에 분기를 두지 않는다
- [ ] AC-15: `common/security/`에 `InvalidTokenException` — `BusinessException` 상속, `code = "AUTH_INVALID_TOKEN"`, `status = HttpStatus.UNAUTHORIZED`, `rule` 없음. **메시지에 토큰 값을 넣지 않는다**
- [ ] AC-16: 만료·서명 오류·형식 오류·타입 불일치가 **전부 같은** `InvalidTokenException`으로 나간다. 구분하지 않은 이유를 주석 한 줄로 남긴다

### Component `jwt-provider` — 검증 (Round 2에서 테스트 1개 허용)
- [ ] AC-17: `src/test/kotlin/com/knowave/bookhub/common/security/JwtProviderImplTest.kt` — Kotest `DescribeSpec`, 기존 `AuthServiceUnitTest` 형식. **스프링 컨텍스트·DB를 쓰지 않고** `JwtProviderImpl(...)`을 생성자로 직접 만든다
- [ ] AC-18: 테스트가 다루는 것 — 컴파일러가 못 잡는 것들만:
      1. access 생성 → `parse` 라운드트립: `id`·`role`·`libraryId`가 그대로 돌아온다
      2. `libraryId = null`(MEMBER) 라운드트립이 null로 돌아온다
      3. refresh 토큰을 `parse`에 넣으면 `InvalidTokenException`
      4. access 토큰을 `parseRefresh`에 넣으면 `InvalidTokenException`
      5. 32바이트 미만 시크릿으로 생성하면 부팅 단계 예외
      6. 서명이 다른 토큰은 `validate`가 `false`, 예외를 던지지 않는다
      7. 만료된 토큰은 `validate`가 `false` (유효기간 음수/0으로 생성)

### Component `app-config`
- [ ] AC-20: `application.yaml`의 `spring.jwt` 블록을 **제거**하고 최상위 `jwt:`로 옮긴다. `secret: ${JWT_SECRET}`, `access-token-validity-seconds: 3600`, `refresh-token-validity-seconds: 1209600`. 기본값(`:`) 없음
- [ ] AC-21: `spring:` 아래에 `jwt` 잔재가 없다

### Component `env-key`
- [ ] AC-22: `.env`의 `JWT_SECURITY=`를 `JWT_SECRET=`으로 바꾼다. **값은 그대로 둔다.** 다른 키(`DB_*`)는 건드리지 않는다

### 전체
- [ ] AC-Z1: `./gradlew compileKotlin compileTestKotlin` → `BUILD SUCCESSFUL`
- [ ] AC-Z2: `./gradlew test --tests '*UnitTest' --tests '*Test'` → 기존 11건 + 신규 테스트 전부 통과
- [ ] AC-Z3: `src/main` 신규는 `JwtProviderImpl.kt`와 토큰 예외뿐. 수정은 `JwtProvider.kt`(메서드 1개 추가)와 `application.yaml`뿐
- [ ] AC-Z4: `UserPrincipal.kt` 변경 0건, `SecurityConfig.kt` 변경 0건, `docs/` 변경 0건
- [ ] AC-Z5: 소스·테스트 어디에도 시크릿 문자열 리터럴이 운영 경로에 없다 (테스트의 더미 시크릿은 허용)

## Assumptions Exposed & Resolved

| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| "application.yaml에 jwt 설정을 추가한다" | 이미 있는가? | **있다.** `spring.jwt.*`로 잘못된 네임스페이스에. 추가가 아니라 **이전**이다 |
| "JWT_SECRET은 .env에 이미 있다" | 실제 키 이름은? | **`JWT_SECURITY`다.** `${JWT_SECRET}`을 그대로 쓰면 부팅 실패. Round 0에서 `.env` 개명으로 확정 |
| ".env를 읽으려면 dotenv 의존성이 필요하다" | 어떻게 로딩되는가? | `spring.config.import: optional:file:.env[.properties]`로 **이미 로딩된다.** 의존성 불필요 |
| "type 클레임을 넣으면 구분된다" | 호출자가 타입을 요구할 수 있는가? | **없다.** 인터페이스가 `parse(token)` 하나뿐. Round 1에서 `parseRefresh` 추가로 해결 |
| "컴파일만으로 충분하다" | JWT에서 컴파일러가 검사하는 게 있는가? | **거의 없다.** 서명·클레임 키·만료가 전부 문자열과 산술. Round 2에서 단위 테스트 1개 허용 |
| "만료와 서명 오류를 구분해야 한다" | 구분해서 클라이언트가 할 수 있는 일이 다른가? | **지금은 없다.** 재발급 엔드포인트가 없다. 단일 코드로 가고 refresh 플로우가 생길 때 재검토 |
| "JwtProvider에 사용처가 있다" | 호출자가 있는가? | **0건.** 인터페이스만 존재. 그래서 인터페이스 변경 비용이 지금 가장 싸다 |

## Technical Context

**이미 존재 (수정 범위 밖)**

| 파일 | 내용 |
|---|---|
| `common/security/UserPrincipal.kt` | `data class UserPrincipal(id: UUID, role: UserRole, libraryId: UUID?)` |
| `common/security/JwtProvider.kt` | 4메서드 인터페이스. `parseRefresh`만 추가 |
| `common/exception/BusinessException.kt` | `code`/`status`/`rule`/`message`. `cause` 없음 |
| `common/exception/GlobalExceptionHandler.kt` | `BusinessException` → 상태코드 자동. **401 처리 코드 추가 불필요** |
| `domains/user/entity/User.kt:80-84` | `UserRole`: `MEMBER` / `LIBRARIAN` / `ADMIN` |
| `build.gradle.kts:31,44,45` | `jjwt-api 0.12.6` + `jjwt-impl`/`jjwt-jackson` (runtimeOnly) |
| `build.gradle.kts:54-58` | Kotest 6.2.5 (`runner-junit5`, `assertions-core`), MockK 1.14.11 |

**jjwt 0.12.x API** (0.11 이하와 다르다 — `parserBuilder`/`setClaims`가 아니다)
```
Jwts.builder().subject(..).claim(..).issuedAt(..).expiration(..).signWith(key).compact()
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
Keys.hmacShaKeyFor(ByteArray)   // 256비트 미만이면 WeakKeyException
```

**테스트 패턴** — `src/test/.../domains/auth/AuthServiceUnitTest.kt`
`class XxxTest : DescribeSpec({ lateinit var sut: ...; beforeEach { ... }; describe("메서드") { it("...") { ... shouldBe ... } } })`

**주의점**
- `common/security`가 `domains.user.entity.UserRole`을 import한다. 이미 그런 구조이므로 새 역전이 아니다.
- 예외는 `common/security/`에 둔다. 프로젝트 directive는 **도메인 전용** 예외를 도메인 패키지에, 공유 인프라(`BusinessException`/`ErrorResponse`/`GlobalExceptionHandler`)만 `common/exception`에 두라고 한다. 토큰 예외는 둘 다 아니고 던지는 쪽(`common/security`) 옆이 자연스럽다.

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| `UserPrincipal` | core (기존) | `id`, `role`, `libraryId?` | 토큰 클레임 ↔ 이 타입 |
| `UserRole` | core (기존) | `MEMBER`/`LIBRARIAN`/`ADMIN` | `UserPrincipal`이 보유 |
| `JwtProvider` | supporting (기존, 확장) | `createAccessToken`, `createRefreshToken`, `validate`, `parse`, `parseRefresh` | → `UserPrincipal` |
| `TokenType` | supporting (신규, 상수) | `access` / `refresh` | `type` 클레임의 값 |
| `InvalidTokenException` | supporting (신규) | `AUTH_INVALID_TOKEN`, 401 | `BusinessException` 상속 |

## Ontology Convergence

| Round | Entity Count | New | Changed | Stable | Stability Ratio |
|-------|-------------|-----|---------|--------|----------------|
| 1 | 5 | 5 | - | - | N/A |
| 2 | 5 | 0 | 0 | 5 | 100% |

## Interview Transcript

<details>
<summary>Full Q&A (Round 0 + 2 rounds)</summary>

### Round 0 — 토폴로지 확인
**Q:** `.env` 키가 `JWT_SECURITY`인데 지시는 `JWT_SECRET`이다. 어느 쪽으로?
**A:** **`JWT_SECRET`으로 통일 — `.env`도 수정.** 컴포넌트 4개 확정.
**Ambiguity:** 미측정

### Round 1 — Constraint Clarity
**Q:** 인터페이스에 타입 구분 자리가 없다. `type` 클레임만 넣을지, 인터페이스를 바꿀지?
**A:** **인터페이스를 바꿈 — 지금 강제.** `parseRefresh` 추가. 금지 해제.
**Ambiguity:** 21.8% → 15.2% (Goal 0.93, Constraints 0.82, Criteria 0.70, Context 0.95)

### Round 2 — Success Criteria
**Q:** 서명·클레임 키·만료는 컴파일러가 검사하지 못하고 DB가 없어 앱도 못 띄운다. 무엇으로 검증할까?
**A:** **단위 테스트 1개 허용.** 스프링 컨텍스트·DB 없이 생성자 직접 호출.
**Ambiguity:** 15.2% → **7.4%** (Goal 0.94, Constraints 0.90, Criteria 0.92, Context 0.95)

</details>

## Open Risks

1. **`.env` 개명이 이 머신에만 적용된다.** `.gitignore:41`에 있어 추적되지 않으므로, 다른 머신이나 CI가 `JWT_SECURITY`를 그대로 두면 **부팅이 실패한다.** 실패 자체는 조용하지 않으니(플레이스홀더 미해결 에러) 발견은 빠르다.
2. **`type` 강제가 지금은 아무것도 보호하지 않는다.** `JwtProvider` 사용처가 0건이라, 실제 효력은 인증 필터와 재발급 엔드포인트가 생겨 `parse`/`parseRefresh`를 올바르게 나눠 부를 때 발생한다. 그때 잘못 부르면 강제가 무력해진다.
3. **시크릿 길이 검사는 바이트 기준이다.** UTF-8에서 ASCII 32자 = 32바이트지만 한글이 섞이면 글자 수와 바이트 수가 어긋난다. 메시지에 바이트 수를 명시해 혼동을 줄인다.

---

**Status: pending approval**
