package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.BookRef
import java.util.UUID

data class BookRefResponse(
    val id: UUID,
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
