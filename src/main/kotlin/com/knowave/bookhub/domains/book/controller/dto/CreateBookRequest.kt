package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CreateBookCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class CreateBookRequest(
    @field:NotBlank(message = "isbn은 필수값입니다.")
    @field:Size(max = 20, message = "isbn은 최대 20자 이하입니다.")
    val isbn: String,

    @field:NotBlank(message = "title은 필수값입니다.")
    @field:Size(max = 300, message = "title은 최대 300자 이하입니다.")
    val title: String,

    @field:NotBlank(message = "author는 필수값입니다.")
    @field:Size(max = 200, message = "author는 최대 200자 이하입니다.")
    val author: String,

    @field:NotBlank(message = "publisher는 필수값입니다.")
    @field:Size(max = 100, message = "publisher는 최대 100자 이하입니다.")
    val publisher: String,

    val publishedAt: LocalDate,

    val categoryId: UUID,

    @field:Size(max = 2000, message = "description은 최대 2000자 이하입니다.")
    val description: String? = null,
) {

    fun toCommand() = CreateBookCommand(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedAt = publishedAt,
        categoryId = categoryId,
        description = description,
    )
}
