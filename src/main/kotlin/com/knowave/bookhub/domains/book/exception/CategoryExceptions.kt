package com.knowave.bookhub.domains.book.exception

import com.knowave.bookhub.common.exception.BusinessException

import org.springframework.http.HttpStatus
import java.util.UUID

class CategoryNotFoundException(categoryId: UUID) : BusinessException(
    code = "CATEGORY_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "분류를 찾을 수 없습니다: $categoryId",
)

class CategoryCircularReferenceException(categoryId: UUID, parentId: UUID?) : BusinessException(
    code = "CATEGORY_CIRCULAR_REFERENCE",
    status = HttpStatus.CONFLICT,
    rule = "C-2",
    message = "자기 자신이나 하위 분류를 상위로 지정할 수 없습니다: categoryId=$categoryId, parentId=$parentId",
)
