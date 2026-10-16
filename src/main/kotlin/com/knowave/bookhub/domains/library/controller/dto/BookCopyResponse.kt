package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.book.controller.dto.BookRefResponse
import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import java.util.UUID

data class BookCopyResponse(
    val id: UUID,
    val callNumber: String,
    val status: BookCopyStatus,
    val book: BookRefResponse,
    val library: LibraryRefResponse,
) {
    companion object {
        fun from(result: BookCopyResult) = BookCopyResponse(
            id = result.id,
            callNumber = result.callNumber,
            status = result.status,
            book = BookRefResponse.from(result.book),
            library = LibraryRefResponse.from(result.library),
        )
    }
}
