package com.knowave.bookhub.domains.book.exception

import com.knowave.bookhub.common.exception.BusinessException

import org.springframework.http.HttpStatus
import java.util.UUID

class BookIsbnDuplicatedException(isbn: String) : BusinessException(
    code = "BOOK_ISBN_DUPLICATED",
    status = HttpStatus.CONFLICT,
    message = "이미 등록된 ISBN입니다: $isbn",
)

class BookNotFoundException(bookId: UUID) : BusinessException(
    code = "BOOK_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "도서를 찾을 수 없습니다: $bookId",
)
