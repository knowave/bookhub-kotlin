package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.CreateBookCopyCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateBookCopyRequest(
    @field:Schema(description = "복본의 원본 도서 ID", example = "9e4f1c2a-7b3d-4e8a-a5c6-1d2e3f4a5b6c")
    val bookId: UUID,

    @field:Schema(description = "복본을 비치할 지점 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val libraryId: UUID,

    @field:Schema(description = "청구기호. 지점 내에서 실물을 식별한다", example = "833.6-무라카-3")
    @field:NotBlank(message = "callNumber는 필수값입니다.")
    @field:Size(max = 50, message = "callNumber는 최대 50자 이하입니다.")
    val callNumber: String,
) {

    fun toCommand() = CreateBookCopyCommand(
        bookId = bookId,
        libraryId = libraryId,
        callNumber = callNumber,
    )
}
