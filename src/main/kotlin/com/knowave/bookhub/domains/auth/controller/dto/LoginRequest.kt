package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.LoginCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "email은 필수값입니다.")
    @field:Email
    val email: String,

    @field:NotBlank(message = "password는 필수값입니다.")
    val password: String,
) {
    fun toCommand() = LoginCommand(
        email = email,
        password = password
    )
}