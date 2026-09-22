package com.knowave.bookhub.domains.user.service.dto

import java.util.UUID

data class CreateLibrarianCommand(
    val email: String,
    val encodedPassword: String,
    val name: String,
    val libraryId: UUID,
)