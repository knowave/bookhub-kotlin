package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import java.util.UUID

data class BookCopyStatusResponse(
    val id: UUID,
    val callNumber: String,
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
