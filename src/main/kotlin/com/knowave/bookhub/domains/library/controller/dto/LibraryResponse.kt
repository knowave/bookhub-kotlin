package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.LibraryRef
import java.util.UUID

data class LibraryResponse(
    val id: UUID,
    val name: String
) {
    companion object {
        fun from(ref: LibraryRef): LibraryResponse =
            LibraryResponse(
                id = ref.id,
                name = ref.name
            )
    }
}
