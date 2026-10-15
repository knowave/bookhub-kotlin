package com.knowave.bookhub.domains.book.service.dto

import com.knowave.bookhub.domains.book.entity.Category
import java.util.UUID

data class CategoryRef(
    val id: UUID,
    val name: String,
) {
    companion object {
        fun from(category: Category) = CategoryRef(
            id = checkNotNull(category.id) { "영속화되지 않은 Category는 변환할 수 없습니다" },
            name = category.name,
        )
    }
}
