package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CategoryResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CategoryResponse(
    @field:Schema(description = "분류 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,

    @field:Schema(description = "분류명", example = "일본소설")
    val name: String,

    @field:Schema(description = "하위 분류 목록. 같은 구조(CategoryResponse)가 재귀적으로 중첩된다")
    val children: List<CategoryResponse>,
) {
    companion object {

        fun from(result: CategoryResult): CategoryResponse = CategoryResponse(
            id = result.id,
            name = result.name,
            children = result.children.map { from(it) },
        )
    }
}
