package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CategoryRef
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CategoryRefResponse(
    @field:Schema(description = "분류 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,

    @field:Schema(description = "분류명", example = "일본소설")
    val name: String
) {
    companion object {
        fun from(ref: CategoryRef): CategoryRefResponse =
            CategoryRefResponse(
                id = ref.id,
                name = ref.name
            )
    }
}
