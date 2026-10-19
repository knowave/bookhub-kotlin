package com.knowave.bookhub.common.security

import com.knowave.bookhub.common.exception.BusinessException

import org.springframework.http.HttpStatus

// 만료·서명 오류·형식 오류·타입 불일치를 구분하지 않는다. 재발급 엔드포인트가 없어 클라이언트가 할 행동이 같고,
// 구분 응답은 "서명은 맞았다"를 알려주는 오라클이 된다. refresh 플로우가 생기면 재검토한다.
class InvalidTokenException : BusinessException(
    code = "AUTH_INVALID_TOKEN",
    status = HttpStatus.UNAUTHORIZED,
    message = "유효하지 않은 토큰입니다.",
)
