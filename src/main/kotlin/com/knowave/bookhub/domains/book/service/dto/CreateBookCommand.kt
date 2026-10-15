package com.knowave.bookhub.domains.book.service.dto

import java.time.LocalDate
import java.util.UUID

data class CreateBookCommand(
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val publishedAt: LocalDate,
    val categoryId: UUID,
    val description: String?
)
