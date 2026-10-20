package com.knowave.bookhub.domains.book.controller.dto

import com.knowave.bookhub.domains.book.service.dto.UpdateCategoryCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class UpdateCategoryRequest(
    @field:Schema(description = "분류명", example = "일본소설")
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(max = 50, message = "name은 최대 50자 이하입니다.")
    val name: String,

    /** PUT은 전체 교체이므로, 생략하면 상위 분류가 유지되지 않고 최상위로 이동한다. */
    @field:Schema(description = "상위 분류. 생략하면 최상위로 이동한다. 상위 분류를 유지하려면 현재 값을 다시 보내야 한다", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val parentId: UUID? = null,
) {

    fun toCommand(categoryId: UUID): UpdateCategoryCommand = UpdateCategoryCommand(
        categoryId = categoryId,
        name = name,
        parentId = parentId,
    )
}
