package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.UpdateContactCommand
import java.util.UUID

data class UpdateLibraryContactRequest(
    val address: String?,
    val phone: String?
) {
    fun toCommand(libraryId: UUID): UpdateContactCommand =
        UpdateContactCommand(
            libraryId = libraryId,
            address = address,
            phone = phone
        )
}
