package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.BookRef
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class BookRefResponse(
    @field:Schema(description = "도서 ID", example = "9e4f1c2a-7b3d-4e8f-9a6b-1c2d3e4f5a6b")
    val id: UUID,

    @field:Schema(description = "도서 제목", example = "노르웨이의 숲")
    val title: String
) {
    companion object {
        fun from(ref: BookRef): BookRefResponse =
            BookRefResponse(
                id = ref.id,
                title = ref.title
            )
    }
}
