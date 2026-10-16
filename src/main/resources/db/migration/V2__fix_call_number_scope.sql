-- 청구기호는 지점 내에서만 유일하다. 전역 유일 제약을 (library_id, call_number) 단위로 바꾼다
alter table book_copies drop constraint uk_copies_call_number;
alter table book_copies add constraint uk_copies_call_number
    unique (library_id, call_number);
