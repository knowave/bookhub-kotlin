package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.BookResult
import java.util.UUID

data class BookResponse(
    val id: UUID,
    val isbn: String,
    val title: String,
    val category: CategoryRefResponse,
    val totalCopies: Int,
) {
    companion object {
        fun from(result: BookResult) = BookResponse(
            id = result.id,
            isbn = result.isbn,
            title = result.title,
            category = CategoryRefResponse.from(result.category),
            totalCopies = result.totalCopies,
        )
    }
}
