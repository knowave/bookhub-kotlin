package com.knowave.bookhub.domains.library.exception

import com.knowave.bookhub.common.exception.BusinessException
import com.knowave.bookhub.domains.library.entity.BookCopyStatus

import org.springframework.http.HttpStatus
import java.util.UUID

class BookCopyNotFoundException(bookCopyId: UUID) : BusinessException(
    code = "BOOK_COPY_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "복본을 찾을 수 없습니다: $bookCopyId",
)

class BookCopyCallNumberDuplicatedException(callNumber: String) : BusinessException(
    code = "BOOK_COPY_CALL_NUMBER_DUPLICATED",
    status = HttpStatus.CONFLICT,
    message = "이미 사용 중인 청구기호입니다: $callNumber",
)

class BookCopyLoanedCannotDamageException(bookCopyId: UUID) : BusinessException(
    code = "BOOK_COPY_LOANED_CANNOT_DAMAGE",
    status = HttpStatus.CONFLICT,
    rule = "B-4",
    message = "대출 중인 복본은 파손 처리할 수 없습니다: $bookCopyId",
)

class BookCopyNotDamagedException(bookCopyId: UUID) : BusinessException(
    code = "BOOK_COPY_NOT_DAMAGED",
    status = HttpStatus.CONFLICT,
    rule = "B-2",
    message = "파손된 복본이 아니어서 수선할 수 없습니다: $bookCopyId",
)

class BookCopyInvalidTransitionException(bookCopyId: UUID, status: BookCopyStatus) : BusinessException(
    code = "BOOK_COPY_INVALID_TRANSITION",
    status = HttpStatus.CONFLICT,
    rule = "B-1",
    message = "현재 상태에서 전이할 수 없습니다: $bookCopyId (${status.label})",
)
