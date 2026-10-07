package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.UpdateNameCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class UpdateLibraryNameRequest(
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(min = 2, max = 20, message = "name은 최소 2자 이상 최대 20자 이하입니다.")
    val name: String,
) {
    fun toCommand(libraryId: UUID): UpdateNameCommand =
        UpdateNameCommand(
            libraryId = libraryId,
            name = name,
        )
}