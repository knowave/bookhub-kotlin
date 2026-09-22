package com.knowave.bookhub.common.exception

import org.springframework.http.HttpStatus
import java.util.UUID

class UserNotFoundException(userId: UUID) : BusinessException(
    code = "USER_NOT_FOUND",
    status = HttpStatus.NOT_FOUND,
    message = "이용자를 찾을 수 없습니다: $userId",
)

class EmailDuplicatedException(email: String) : BusinessException(
    code = "USER_EMAIL_DUPLICATED",
    status = HttpStatus.CONFLICT,
    rule = "M-4",
    message = "이미 사용 중인 이메일입니다: $email",
)

class UserNotPendingException(userId: UUID) : BusinessException(
    code = "USER_NOT_PENDING",
    status = HttpStatus.CONFLICT,
    rule = "M-2",
    message = "승인 대기 상태가 아닙니다: $userId",
)