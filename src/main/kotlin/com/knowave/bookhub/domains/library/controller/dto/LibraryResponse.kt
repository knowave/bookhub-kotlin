package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.LibraryResult
import java.util.UUID

data class LibraryResponse(
    val id: UUID,
    val name: String,
    val address: String,
    val phone: String
) {
    companion object {

        fun from(result: LibraryResult) = LibraryResponse(
            id = result.id,
            name = result.name,
            address = result.address,
            phone = result.phone
        )
    }
}
