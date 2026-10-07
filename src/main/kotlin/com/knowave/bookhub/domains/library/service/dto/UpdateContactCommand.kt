package com.knowave.bookhub.domains.library.service.dto

import java.util.UUID

data class UpdateContactCommand(
    val libraryId: UUID,
    val address: String?,
    val phone: String?
)
