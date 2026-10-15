package com.knowave.bookhub.domains.book.service.dto

import com.knowave.bookhub.domains.book.entity.Book
import java.time.LocalDate
import java.util.UUID

data class BookResult(
    val id: UUID,
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val publishedAt: LocalDate,
    val description: String?,
    val category: CategoryRef,
    val totalCopies: Int,
) {
    companion object {
        fun from(book: Book, totalCopies: Int) = BookResult(
            id = checkNotNull(book.id) { "영속화되지 않은 Book은 변환할 수 없습니다" },
            isbn = book.isbn,
            title = book.title,
            author = book.author,
            publisher = book.publisher,
            publishedAt = book.publishedAt,
            description = book.description,
            category = CategoryRef.from(book.category),
            totalCopies = totalCopies,
        )
    }
}
