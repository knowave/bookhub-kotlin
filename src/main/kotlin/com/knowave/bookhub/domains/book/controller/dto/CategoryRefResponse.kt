package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CategoryRef
import java.util.UUID

data class CategoryRefResponse(
    val id: UUID,
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
