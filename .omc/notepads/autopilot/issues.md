## 2026-10-15 보안 블로커 (미조치, 사용자 지시로 보류) — CategoryController 작업 중 발견

**실패 서명:** security-reviewer REJECT, CRITICAL 1 + HIGH 1. 이번 변경이 만든 문제가 아니며 전부 기존 설정.

1. **CRITICAL — H2 콘솔 운영 노출 → RCE.** 세 조건 동시 성립을 직접 확인함:
   - `build.gradle.kts:30` `implementation("org.springframework.boot:spring-boot-h2console")` (39행은 devtools에 `developmentOnly`를 제대로 씀)
   - `src/main/resources/application.yaml:26-28` `h2.console.enabled: true` (프로파일 구분 없음)
   - `src/main/kotlin/com/knowave/bookhub/common/config/SecurityConfig.kt:15` `anyRequest().permitAll()`
   H2 콘솔은 접속 JDBC URL을 사용자가 입력하므로 `INIT=RUNSCRIPT FROM 'http://...'`로 임의 코드 실행 가능.

2. **HIGH — `anyRequest().permitAll()`.** 문서상 ADMIN 전용인 `POST`/`PUT /api/v1/categories`가 미인증 개방. "JWT 전까지 어차피 안 막혀 있다"가 아니라 "아무나 쓸 수 있다"가 현재 상태. `PUT`은 전체 교체라 `parentId` 생략만으로 하위 트리를 최상위로 끌어올릴 수 있음.

**영향 범위:** 카테고리만이 아니라 Library/User/Auth 컨트롤러 전부 동일.

**보류 사유:** 사용자가 "Security는 나중에 구현할 거라서 건들말고 주석만 달아놓아"라고 명시. 컨트롤러에 `// TODO: ADMIN 권한 검증 필요 (→ P-6)` 주석만 남김.

**재개 경로:** P-6(권한 검증) 작업 착수 시 1번을 먼저 처리할 것 — 1번은 JWT와 무관하게 지금 당장 고칠 수 있고, 배포되는 순간 열림.
