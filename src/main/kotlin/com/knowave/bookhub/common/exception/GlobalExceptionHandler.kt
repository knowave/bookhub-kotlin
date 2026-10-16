package com.knowave.bookhub.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        e: BusinessException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.status).body(
            ErrorResponse(
                code = e.code,
                message = e.message,
                rule = e.rule,
                path = request.requestURI,
            )
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "VALIDATION_FAILED",
                message = message,
                path = request.requestURI,
            )
        )
    }

    /**
     * 본문을 읽지 못했거나 경로 변수의 타입이 맞지 않은 요청.
     *
     * non-null 코틀린 프로퍼티는 필드가 비면 빈 검증([MethodArgumentNotValidException])에
     * 닿기 전에 역직렬화 단계에서 먼저 실패한다. 이 핸들러가 없으면 그 경우만
     * [ErrorResponse] 계약을 벗어나 스프링 기본 응답으로 나간다.
     *
     * 예외 메시지는 내부 타입명을 담으므로 클라이언트에 그대로 내보내지 않는다.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun handleMalformedRequest(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "INVALID_REQUEST",
                message = "요청 형식이 올바르지 않습니다.",
                path = request.requestURI,
            )
        )
}