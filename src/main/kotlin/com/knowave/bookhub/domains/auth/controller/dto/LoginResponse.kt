package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.LoginResult

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun from(result: LoginResult) = LoginResponse(
            accessToken =  result.accessToken,
            refreshToken = result.refreshToken
        )
    }
}