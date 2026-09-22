package com.knowave.bookhub.domains.auth.service.dto

import com.knowave.bookhub.domains.user.service.dto.CreateMemberCommand

data class SignUpMemberCommand(
    val email: String,
    val password: String,
    val name: String
) {
    fun toCreateCommand(encodedPassword: String) = CreateMemberCommand(
        email = email,
        encodedPassword = encodedPassword,
        name = name,
    )
}
