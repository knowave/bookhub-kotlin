package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.SignUpLibrarianCommand
import jakarta.validation.constraints.*
import java.util.UUID

data class SignUpLibrarianRequest(
    @field:NotBlank(message = "email은 필수값입니다.")
    @field:Email
    val email: String,

    @field:NotBlank(message = "password는 필수값입니다.")
    @field:Pattern(
        regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
        message = "비밀번호는 8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요."
    )
    val password: String,

    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(min=2, max = 12, message = "이름은 최소 2자 이상 최대 12자 이하입니다.")
    val name: String,

    val libraryId: UUID,
) {
    fun toCommand() = SignUpLibrarianCommand(
        email = email,
        password = password,
        name = name,
        libraryId = libraryId,
    )
}
