package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CategoryResult
import java.util.UUID

data class CategoryResponse(
    val id: UUID,
    val name: String,
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
