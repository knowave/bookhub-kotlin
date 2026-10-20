package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CreateBookCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class CreateBookRequest(
    @field:Schema(description = "ISBN. 이미 등록된 ISBN은 사용할 수 없다", example = "9788983920683")
    @field:NotBlank(message = "isbn은 필수값입니다.")
    @field:Size(max = 20, message = "isbn은 최대 20자 이하입니다.")
    val isbn: String,

    @field:Schema(description = "도서 제목", example = "노르웨이의 숲")
    @field:NotBlank(message = "title은 필수값입니다.")
    @field:Size(max = 300, message = "title은 최대 300자 이하입니다.")
    val title: String,

    @field:Schema(description = "저자명", example = "무라카미 하루키")
    @field:NotBlank(message = "author는 필수값입니다.")
    @field:Size(max = 200, message = "author는 최대 200자 이하입니다.")
    val author: String,

    @field:Schema(description = "출판사명", example = "민음사")
    @field:NotBlank(message = "publisher는 필수값입니다.")
    @field:Size(max = 100, message = "publisher는 최대 100자 이하입니다.")
    val publisher: String,

    @field:Schema(description = "출간일 (yyyy-MM-dd)", example = "2013-09-01")
    val publishedAt: LocalDate,

    @field:Schema(description = "소속 분류의 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val categoryId: UUID,

    @field:Schema(description = "도서 소개. 선택 입력", example = "상실과 사랑을 그린 장편소설")
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
