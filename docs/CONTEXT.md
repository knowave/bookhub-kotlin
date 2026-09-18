## 프로젝트
bookhub — 도서관 대출 관리 시스템. Kotlin + Spring Boot 학습용 개인 프로젝트.
기술 스택: Kotlin 2.3, Spring Boot 4.1, JPA/Hibernate, PostgreSQL, Kotest, JWT

## 도메인 경계
- user: User
- book: Book, Category
- library: Library, BookCopy
- loan: Loan

## 엔티티 (확정)
모든 엔티티는 BaseEntity 상속 (id: UUID, createdAt/updatedAt: Instant)

- Book: isbn, title, author, publisher, publishedAt, category, description?
  → 서지정보. 물리적 실물이 아닌 카탈로그 단위
- BookCopy: callNumber, book, library, status
  → 서가에 꽂힌 실제 한 권. 대출의 대상
  → status: AVAILABLE | LOANED | DAMAGED | LOST
- Library: name, address, phone
- Category: name, parent? (자기참조 계층 구조)
- User: email, password, name, role, status, library?
  → role: MEMBER | LIBRARIAN | ADMIN
  → status: PENDING | ACTIVE
  → library: Library? (nullable) — 사서의 소속 지점
    MEMBER/ADMIN은 null, LIBRARIAN은 필수
- Loan: user, bookCopy, loanedAt, dueDate, status, closedAt?, extensionCount
  → closedAt: 대출 종료일. 반납이든 분실이든 종료 시점을 기록한다
    status가 LOANED인 동안은 null
  → status: LOANED | RETURNED | LOST
    연체는 저장하지 않고 계산한다 (→ ADR-0007). LOST는 복본 분실로 인한 종료

## 도메인 규칙 (확정)
[대출]
- 대출 기간 14일
- 동시 대출 한도 3권
- 연체 중인 대출이 1건이라도 있으면 신규 대출 불가
- 연장은 7일씩 최대 2회, 연체 중에는 불가
- 대출/반납 처리는 사서만 수행
- 연장은 회원 본인 또는 사서가 수행한다
  → 실물이 오가지 않는 데이터 변경이며, 필요한 검증이 모두 저장된 값으로 판단 가능하다
- 회원이 연장할 때는 해당 대출이 본인 것인지 반드시 확인한다
- 연체는 상태로 저장하지 않고 계산으로만 처리한다 (status == LOANED && dueDate < 오늘)
  → LoanStatus에 OVERDUE를 두지 않는다. 연체 전환 배치도 만들지 않는다
  → 저장하면 배치 시점과 실제 시점 사이에 불일치가 생겨 진실이 두 개가 된다
  → 연체는 행위로 전이되는 상태가 아니라 시간이 지나면 자동으로 성립하는 조건이다
  → 응답 DTO에 isOverdue, overdueDays를 별도 필드로 담는다
  → 결정 기록: docs/adr/0007-overdue-as-calculation.md
- 대출 대상은 AVAILABLE 상태의 복본에 한정한다
- 대출은 반납(RETURNED) 또는 복본 분실(LOST)로 종료된다
  → 분실은 사서의 명시적 판정으로 전이되므로 연체와 달리 상태로 저장한다

[복본 상태]
- 허용 전이는 6가지뿐이다
  AVAILABLE → LOANED (대출)        LOANED → AVAILABLE (반납)
  LOANED → LOST (대출 중 분실)      AVAILABLE → DAMAGED (파손 판정)
  AVAILABLE → LOST (서가 분실)      DAMAGED → AVAILABLE (수선 완료)
- LOANED → DAMAGED는 금지. 먼저 반납 후 파손 처리한다
  → 파손된 책은 실물이 돌아오므로 반납이 선행 가능하다
- 분실은 대출 중에도 처리할 수 있다
  → 분실된 책은 반납 자체가 불가능하다
- 대출 중인 복본을 분실 처리하면 해당 대출도 LOST로 종료된다
  → 복본만 LOST로 바꾸면 대출이 LOANED로 남아 회원의 대출 한도를 영구 점유한다
- LOST는 최종 상태, DAMAGED는 수선으로 복귀 가능
→ 상세 규칙: docs/02-requirements.md 4.5절 (B-1 ~ B-5)

[회원]
- PENDING 상태인 계정은 승인 대기 중인 사서뿐이다.
  MEMBER와 ADMIN은 생성 시점부터 항상 ACTIVE다
- 일반 회원: 가입 즉시 ACTIVE
- 사서: 가입 시 PENDING, 관리자 승인 후 ACTIVE
- PENDING 상태는 로그인 불가
- 관리자: 초기 SQL로 생성 (멱등)
- LIBRARIAN은 반드시 소속 지점을 가진다. MEMBER/ADMIN은 소속이 없다(null)
  → 사서 권한은 언제나 특정 지점에 대한 권한이다
- 사서 회원가입 시 소속 지점을 지정하고, 관리자 승인 시 소속을 확인한다
- 기본 지점: 초기 SQL로 생성 (멱등)
  → 사서 가입 시 선택할 지점이 최소 하나는 존재해야 한다
  → 지점 개설은 ADMIN 권한인데 지점이 없으면 첫 사서가 가입할 수 없다

[권한]
- 권한은 누적이 아니라 분기한다. LIBRARIAN과 ADMIN은 서로 포함 관계가 아니다
  → ADMIN은 사서의 상위 권한자가 아니라 운영을 관리하는 다른 역할이다
  → 창구 업무는 현장 사서의 일이며 관리자가 대신 수행하지 않는다
- 공통(세 역할 전부): 도서 검색, 본인 대출 이력 조회, 본인 대출 연장
- LIBRARIAN 전용: 도서 등록, 복본 등록, 복본 파손/수선/분실 처리,
                 대출/반납 처리, 대출 연장 대행, 지점 대출 현황 조회,
                 지점 연락 정보 수정
- ADMIN 전용: 사서 승인, 지점 개설, 지점명 수정, 카테고리 생성/수정
- 본인 소유 리소스 행위는 역할 검사와 별도로 소유권을 검증한다
- 사서의 권한 검사는 두 층이다: 역할 == LIBRARIAN 그리고 대상의 지점 == 사서 소속
  → LIBRARIAN 전용 규칙이다. ADMIN은 제약을 면제받는 것이 아니라
    해당 업무 자체를 수행하지 않는다
  → 적용: 복본 등록/상태변경, 대출/반납 처리, 대출 연장 대행,
         지점 대출 현황 조회(자기 지점만), 지점 연락 정보 수정
  → 역할만 검사하면 A지점 사서가 B지점 복본을 대출 처리할 수 있다
- 예외: 도서 등록(Book)은 소속과 무관하다. 소속 제약은 BookCopy부터 적용된다
  → Book은 전 지점이 공유하는 공통 카탈로그다

[마스터 데이터]
- Library(지점): 개설은 ADMIN. 수정은 필드에 따라 갈린다
  → 지점명(name)은 ADMIN, 연락 정보(address/phone)는 소속 LIBRARIAN
  → 지점명은 조직이 정하는 정체성이고 주소·전화번호는 현장 운영 정보다.
    각 정보를 가장 정확히 아는 주체가 다르므로 수정 권한도 나뉜다
- Category(분류): 생성/수정 모두 ADMIN
  → 전 지점이 공유하는 분류 체계라 개별 사서가 바꾸면 일관성이 깨진다
- 카테고리 상위 변경 시 순환 참조 불가 (자기 자신·자신의 하위를 상위로 지정 금지)
  → 순환이 생기면 계층 탐색이 무한 재귀에 빠진다

[검색]
- 도서 검색은 제목·저자·ISBN으로 하고, 지점·대출 가능 여부·카테고리로 필터링한다
- 카테고리 필터는 하위 카테고리를 포함한다
  → 이용자는 "문학"을 찾을 때 그 아래 세부 분류까지 함께 보기를 기대한다
- 대출 가능 여부 필터는 AVAILABLE 복본이 1권 이상인 Book만 남긴다

[1차 범위 제외]
예약, 연체 벌점, 지점 간 이동, 저자 엔티티 분리,
연체 상태 전환 배치, 복본 폐기/삭제, 비밀번호 재설정/이메일 인증
→ 상세 이유: docs/02-requirements.md 5장

## 작성 규칙
- 한국어로 작성
- 결정뿐 아니라 "왜"와 "포기한 것"을 함께 기록
- 코드를 문서에 복붙하지 않는다. 코드는 저장소에, 문서는 의도를 담는다
- 위에 없는 정보는 추측해서 채우지 말고 질문할 것
- 표와 목록을 적극 활용해 스캔 가능하게