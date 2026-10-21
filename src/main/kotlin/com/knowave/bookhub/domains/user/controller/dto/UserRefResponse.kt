package com.knowave.bookhub.domains.user.controller.dto

import com.knowave.bookhub.domains.user.service.dto.UserRef
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UserRefResponse(
    @field:Schema(description = "이용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,

    @field:Schema(description = "이용자명", example = "김독자")
    val name: String
) {
    companion object {
        fun from(ref: UserRef): UserRefResponse =
            UserRefResponse(
                id = ref.id,
                name = ref.name
            )
    }
}
