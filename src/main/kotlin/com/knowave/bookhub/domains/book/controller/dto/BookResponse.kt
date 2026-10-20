package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.BookResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class BookResponse(
    @field:Schema(description = "도서 ID", example = "9e4f1c2a-7b3d-4e8f-9a6b-1c2d3e4f5a6b")
    val id: UUID,

    @field:Schema(description = "ISBN", example = "9788983920683")
    val isbn: String,

    @field:Schema(description = "도서 제목", example = "노르웨이의 숲")
    val title: String,

    @field:Schema(description = "소속 분류 요약")
    val category: CategoryRefResponse,

    @field:Schema(description = "등록된 복본 수. 생성 직후에는 항상 0", example = "0")
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
