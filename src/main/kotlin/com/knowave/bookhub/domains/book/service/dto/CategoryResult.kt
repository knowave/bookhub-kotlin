package com.knowave.bookhub.domains.book.service.dto

import com.knowave.bookhub.domains.book.entity.Category
import java.util.UUID

data class CategoryResult(
    val id: UUID,
    val name: String,
    val children: List<CategoryResult>,
) {
    companion object {
        fun from(category: Category): CategoryResult = CategoryResult(
            id = checkNotNull(category.id) { "영속화되지 않은 Category는 변환할 수 없습니다." },
            name = category.name,
            children = category.children.map { from(it) },
        )
    }
}
