package com.knowave.bookhub.domains.auth.service.dto

import com.knowave.bookhub.domains.user.service.dto.CreateLibrarianCommand
import java.util.UUID

data class SignUpLibrarianCommand(
    val email: String,
    val password: String,
    val name: String,
    val libraryId: UUID,
) {
    fun toCreateCommand(encodedPassword: String) = CreateLibrarianCommand(
        email = email,
        encodedPassword = encodedPassword,
        name = name,
        libraryId = libraryId,
    )
}
