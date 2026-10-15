package com.knowave.bookhub.domains.library.exception

import com.knowave.bookhub.common.exception.BusinessException

import org.springframework.http.HttpStatus
import java.util.UUID

class LibraryNotFoundException(libraryId: UUID) : BusinessException(
    code = "LIBRARY_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "지점을 찾을 수 없습니다: $libraryId",
)