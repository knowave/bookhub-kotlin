package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.LibraryRef
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class LibraryRefResponse(
    @field:Schema(description = "지점 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,
    @field:Schema(description = "지점명", example = "중앙도서관")
    val name: String
) {
    companion object {
        fun from(ref: LibraryRef): LibraryRefResponse =
            LibraryRefResponse(
                id = ref.id,
                name = ref.name
            )
    }
}
