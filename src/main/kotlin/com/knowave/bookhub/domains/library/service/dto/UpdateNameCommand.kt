package com.knowave.bookhub.domains.library.service.dto

import java.util.UUID

data class UpdateNameCommand(
    val libraryId: UUID,
    val name: String
)
