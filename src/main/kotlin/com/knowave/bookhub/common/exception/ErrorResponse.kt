package com.knowave.bookhub.common.exception

import java.time.Instant

data class ErrorResponse(
    val code: String,
    val message: String,
    val rule: String? = null,
    val timestamp: Instant = Instant.now(),
    val path: String,
)
