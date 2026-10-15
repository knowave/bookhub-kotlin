package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CreateCategoryCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateCategoryRequest(
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(max = 50, message = "name은 최대 50자 이하입니다.")
    val name: String,

    val parentId: UUID? = null,
) {

    fun toCommand() = CreateCategoryCommand(
        name = name,
        parentId = parentId,
    )
}
