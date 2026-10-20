package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class BookCopyLostResponse(
    @field:Schema(description = "복본 ID", example = "d5e6f7a8-1b2c-4d3e-9f40-5a6b7c8d9e0f")
    val id: UUID,
    @field:Schema(description = "청구기호. 지점 내에서 실물을 식별한다", example = "833.6-무라카-3")
    val callNumber: String,
    @field:Schema(description = "변경된 복본 상태 (AVAILABLE, LOANED, DAMAGED, LOST)", example = "LOST")
    val status: BookCopyStatus,
    /** B-5 미구현이라 항상 null. 담을 수 있는 값이 없음을 타입으로 고정한다. Loan 도메인 구현 시 실제 타입으로 바꾼다. */
    @field:Schema(description = "함께 종료된 대출. 현재는 항상 null (B-5 미구현)")
    val closedLoan: Nothing? = null,
) {
    companion object {
        fun from(result: BookCopyResult) = BookCopyLostResponse(
            id = result.id,
            callNumber = result.callNumber,
            status = result.status,
        )
    }
}
