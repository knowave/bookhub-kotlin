package com.knowave.bookhub.domains.book.service.dto

import com.knowave.bookhub.domains.book.entity.Book
import java.util.UUID

data class BookRef(
    val id: UUID,
    val title: String,
) {
    companion object {
        fun from(book: Book) = BookRef(
            id = checkNotNull(book.id) { "영속화되지 않은 Book은 변환할 수 없습니다" },
            title = book.title,
        )
    }
}
