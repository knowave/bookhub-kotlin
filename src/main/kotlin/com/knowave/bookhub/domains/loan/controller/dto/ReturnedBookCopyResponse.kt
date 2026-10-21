package com.knowave.bookhub.domains.loan.controller.dto

import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyRef
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ReturnedBookCopyResponse(
    @field:Schema(description = "복본 ID", example = "d5e6f7a8-1b2c-4d3e-9f40-5a6b7c8d9e0f")
    val id: UUID,

    @field:Schema(description = "복본 상태 (AVAILABLE, LOANED, DAMAGED, LOST)", example = "AVAILABLE")
    val status: BookCopyStatus,
) {
    companion object {
        fun from(ref: BookCopyRef) = ReturnedBookCopyResponse(
            id = ref.id,
            status = ref.status,
        )
    }
}
