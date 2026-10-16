package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import java.util.UUID

data class BookCopyLostResponse(
    val id: UUID,
    val callNumber: String,
    val status: BookCopyStatus,
    /** B-5 미구현이라 항상 null. 담을 수 있는 값이 없음을 타입으로 고정한다. Loan 도메인 구현 시 실제 타입으로 바꾼다. */
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
