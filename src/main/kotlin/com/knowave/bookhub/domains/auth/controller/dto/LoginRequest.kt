package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.LoginCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Schema(description = "로그인 아이디로 쓰는 이메일", example = "member@bookhub.io")
    @field:NotBlank(message = "email은 필수값입니다.")
    @field:Email
    val email: String,

    @field:Schema(description = "로그인 비밀번호", example = "Password1!")
    @field:NotBlank(message = "password는 필수값입니다.")
    val password: String,
) {
    fun toCommand() = LoginCommand(
        email = email,
        password = password
    )
}
