package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.CreateBookCopyCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateBookCopyRequest(
    val bookId: UUID,

    val libraryId: UUID,

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
