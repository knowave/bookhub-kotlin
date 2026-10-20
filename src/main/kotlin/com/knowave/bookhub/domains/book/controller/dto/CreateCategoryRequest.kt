package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.CreateCategoryCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateCategoryRequest(
    @field:Schema(description = "분류명", example = "일본소설")
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(max = 50, message = "name은 최대 50자 이하입니다.")
    val name: String,

    @field:Schema(description = "상위 분류. 생략하면 최상위 분류가 된다", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val parentId: UUID? = null,
) {

    fun toCommand() = CreateCategoryCommand(
        name = name,
        parentId = parentId,
    )
}
