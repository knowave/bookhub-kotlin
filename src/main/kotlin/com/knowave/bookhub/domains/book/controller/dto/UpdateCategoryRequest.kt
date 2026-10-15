package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.UpdateCategoryCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class UpdateCategoryRequest(
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(max = 50, message = "name은 최대 50자 이하입니다.")
    val name: String,

    /** PUT은 전체 교체이므로, 생략하면 상위 분류가 유지되지 않고 최상위로 이동한다. */
    val parentId: UUID? = null,
) {

    fun toCommand(categoryId: UUID): UpdateCategoryCommand = UpdateCategoryCommand(
        categoryId = categoryId,
        name = name,
        parentId = parentId,
    )
}
