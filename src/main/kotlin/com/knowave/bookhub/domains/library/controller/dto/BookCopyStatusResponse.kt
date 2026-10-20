package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class BookCopyStatusResponse(
    @field:Schema(description = "복본 ID", example = "d5e6f7a8-1b2c-4d3e-9f40-5a6b7c8d9e0f")
    val id: UUID,
    @field:Schema(description = "청구기호. 지점 내에서 실물을 식별한다", example = "833.6-무라카-3")
    val callNumber: String,
    @field:Schema(description = "변경된 복본 상태 (AVAILABLE, LOANED, DAMAGED, LOST)", example = "DAMAGED")
    val status: BookCopyStatus,
) {
    companion object {
        fun from(result: BookCopyResult) = BookCopyStatusResponse(
            id = result.id,
            callNumber = result.callNumber,
            status = result.status,
        )
    }
}
