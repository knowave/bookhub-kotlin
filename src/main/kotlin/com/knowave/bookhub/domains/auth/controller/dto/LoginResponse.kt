package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.LoginResult
import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    @field:Schema(description = "API 요청에 쓰는 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @field:Schema(description = "액세스 토큰 재발급에 쓰는 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String
) {
    companion object {
        fun from(result: LoginResult) = LoginResponse(
            accessToken =  result.accessToken,
            refreshToken = result.refreshToken
        )
    }
}
