package com.knowave.bookhub.domains.user.service.dto

data class CreateMemberCommand(
    val email: String,
    val encodedPassword: String,
    val name: String,
)