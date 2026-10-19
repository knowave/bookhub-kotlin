package com.knowave.bookhub.domains.auth.service.dto

data class LoginResult(
    val accessToken: String,
    val refreshToken: String
)
