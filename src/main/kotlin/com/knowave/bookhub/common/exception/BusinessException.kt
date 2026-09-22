package com.knowave.bookhub.common.exception

import org.springframework.http.HttpStatus

abstract class BusinessException(
    val code: String,
    val status: HttpStatus,
    val rule: String? = null,
    override val message: String,
) : RuntimeException(message)