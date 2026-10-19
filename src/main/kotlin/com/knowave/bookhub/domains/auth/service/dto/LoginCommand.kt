package com.knowave.bookhub.domains.auth.service.dto

data class LoginCommand(
    val email: String,
    val password: String
)