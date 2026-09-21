-- ============================================================
-- V1: 초기 스키마
-- ============================================================
-- 테이블 생성 순서는 외래키 의존 방향을 따른다.
-- libraries / categories → books → book_copies → users → loans
-- ============================================================


-- ------------------------------------------------------------
-- libraries : 도서관 지점
-- ------------------------------------------------------------
create table libraries (
                           id          uuid         not null,
                           name        varchar(100) not null,
                           address     varchar(300) not null,
                           phone       varchar(20)  not null,
                           created_at  timestamp(6) with time zone not null,
                           updated_at  timestamp(6) with time zone not null,

                           constraint pk_libraries primary key (id)
);

comment on table libraries            is '도서관 지점';
comment on column libraries.name      is '지점명';
comment on column libraries.address   is '지점 주소';
comment on column libraries.phone     is '대표 전화번호';


-- ------------------------------------------------------------
-- categories : 도서 분류 (자기참조 계층)
-- ------------------------------------------------------------
create table categories (
                            id          uuid        not null,
                            name        varchar(50) not null,
                            parent_id   uuid,
                            created_at  timestamp(6) with time zone not null,
                            updated_at  timestamp(6) with time zone not null,

                            constraint pk_categories        primary key (id),
                            constraint fk_categories_parent foreign key (parent_id) references categories (id)
);

comment on table categories             is '도서 분류. 자기참조로 계층 구조를 표현한다';
comment on column categories.name       is '분류명';
comment on column categories.parent_id  is '상위 분류. 최상위 분류는 null';


-- ------------------------------------------------------------
-- books : 도서 서지정보
-- ------------------------------------------------------------
create table books (
                       id            uuid          not null,
                       isbn          varchar(20)   not null,
                       title         varchar(300)  not null,
                       author        varchar(200)  not null,
                       publisher     varchar(100)  not null,
                       published_at  date          not null,
                       description   varchar(2000),
                       category_id   uuid          not null,
                       created_at    timestamp(6) with time zone not null,
                       updated_at    timestamp(6) with time zone not null,

                       constraint pk_books          primary key (id),
                       constraint uk_books_isbn     unique (isbn),
                       constraint fk_books_category foreign key (category_id) references categories (id)
);

comment on table books              is '도서 서지정보. 물리적 실물이 아닌 카탈로그 단위';
comment on column books.isbn        is '국제표준도서번호. 도서를 식별하는 고유값';
comment on column books.title       is '도서명';
comment on column books.author      is '저자명. 공저는 쉼표로 구분';
comment on column books.publisher   is '출판사명';
comment on column books.published_at is '출간일';
comment on column books.description is '도서 소개. 등록되지 않을 수 있다';
comment on column books.category_id is '도서 분류';

create index idx_books_title on books (title);


-- ------------------------------------------------------------
-- book_copies : 서가에 있는 실물 한 권
-- ------------------------------------------------------------
create table book_copies (
                             id           uuid        not null,
                             call_number  varchar(50) not null,
                             status       varchar(20) not null,
                             book_id      uuid        not null,
                             library_id   uuid        not null,
                             created_at   timestamp(6) with time zone not null,
                             updated_at   timestamp(6) with time zone not null,

                             constraint pk_book_copies              primary key (id),
                             constraint uk_copies_call_number       unique (call_number),
                             constraint fk_copies_book              foreign key (book_id)    references books (id),
                             constraint fk_copies_library           foreign key (library_id) references libraries (id),
                             constraint ck_copies_status            check (status in ('AVAILABLE', 'LOANED', 'DAMAGED', 'LOST'))
);

comment on table book_copies              is '서가에 꽂혀 있는 실제 도서 한 권. 대출의 대상이 되는 단위';
comment on column book_copies.call_number is '청구기호. 지점 내에서 실물을 식별하는 값';
comment on column book_copies.status      is '복본 상태. AVAILABLE만 대출 가능';
comment on column book_copies.book_id     is '이 복본이 어떤 도서인지';
comment on column book_copies.library_id  is '이 복본이 비치된 지점';

-- 도서 검색(UC-06)의 핵심 인덱스: 지점 + 도서 + 상태로 대출 가능 여부를 판정한다
create index idx_copies_lookup on book_copies (library_id, book_id, status);


-- ------------------------------------------------------------
-- users : 서비스 이용자
-- ------------------------------------------------------------
create table users (
                       id          uuid         not null,
                       email       varchar(100) not null,
                       password    varchar(100) not null,
                       name        varchar(50)  not null,
                       role        varchar(20)  not null,
                       status      varchar(20)  not null,
                       library_id  uuid,
                       created_at  timestamp(6) with time zone not null,
                       updated_at  timestamp(6) with time zone not null,

                       constraint pk_users          primary key (id),
                       constraint uk_users_email    unique (email),
                       constraint fk_users_library  foreign key (library_id) references libraries (id),
                       constraint ck_users_role     check (role   in ('MEMBER', 'LIBRARIAN', 'ADMIN')),
                       constraint ck_users_status   check (status in ('PENDING', 'ACTIVE'))
);

comment on table users             is '서비스 이용자';
comment on column users.email      is '이메일. 로그인 식별자로 사용한다';
comment on column users.password   is '암호화된 비밀번호. 평문을 저장하지 않는다';
comment on column users.name       is '이용자명';
comment on column users.role       is '권한. 접근 가능한 기능을 결정한다';
comment on column users.status     is '계정 상태. PENDING은 로그인 불가';
comment on column users.library_id is '사서의 소속 지점. MEMBER·ADMIN은 null';


-- ------------------------------------------------------------
-- loans : 대출 건
-- ------------------------------------------------------------
create table loans (
                       id               uuid        not null,
                       loaned_at        date        not null,
                       due_date         date        not null,
                       closed_at        date,
                       status           varchar(20) not null,
                       extension_count  integer     not null,
                       user_id          uuid        not null,
                       book_copy_id     uuid        not null,
                       created_at       timestamp(6) with time zone not null,
                       updated_at       timestamp(6) with time zone not null,

                       constraint pk_loans            primary key (id),
                       constraint fk_loans_user       foreign key (user_id)      references users (id),
                       constraint fk_loans_book_copy  foreign key (book_copy_id) references book_copies (id),
                       constraint ck_loans_status     check (status in ('LOANED', 'RETURNED', 'LOST'))
);

comment on table loans                 is '대출 건. 대출부터 반납까지가 하나의 행으로 유지된다';
comment on column loans.loaned_at      is '대출 시작일';
comment on column loans.due_date       is '반납 예정일. 연장 시 갱신된다';
comment on column loans.closed_at      is '대출 종료일. 반납이든 분실이든 종료 시점을 기록한다. 종료 전에는 null';
comment on column loans.status         is '대출 상태. 연체는 저장하지 않고 due_date와 비교해 계산한다';
comment on column loans.extension_count is '연장 횟수';
comment on column loans.user_id        is '대출한 이용자';
comment on column loans.book_copy_id   is '대출된 복본';

create index idx_loans_user_status on loans (user_id, status);

-- 연체 조회(UC-16)의 핵심 인덱스: status = 'LOANED' and due_date < 오늘
create index idx_loans_overdue on loans (status, due_date);