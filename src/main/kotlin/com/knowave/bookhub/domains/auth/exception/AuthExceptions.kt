package com.knowave.bookhub.domains.auth.exception

import com.knowave.bookhub.common.exception.BusinessException
import org.springframework.http.HttpStatus

class InvalidCredentialsException : BusinessException(
    code = "AUTH_INVALID_CREDENTIALS",
    status = HttpStatus.UNAUTHORIZED,
    message = "인증 정보가 잘못 되었습니다."
)