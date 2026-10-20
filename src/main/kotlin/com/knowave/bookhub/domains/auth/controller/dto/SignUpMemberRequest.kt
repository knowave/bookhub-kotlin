package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.auth.service.dto.SignUpMemberCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpMemberRequest(
    @field:Schema(description = "로그인 아이디로 쓸 이메일", example = "member@bookhub.io")
    @field:NotBlank(message = "email은 필수값입니다.")
    @field:Email
    val email: String,

    @field:Schema(description = "로그인 비밀번호", example = "Password1!")
    @field:NotBlank(message = "password는 필수값입니다.")
    @field:Pattern(
        regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
        message = "비밀번호는 8~20자 영문 대 소문자, 숫자, 특수문자를 사용하세요."
    )
    val password: String,

    @field:Schema(description = "이용자 이름", example = "홍길동")
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(min=2, max = 12, message = "이름은 최소 2자 이상 최대 12자 이하입니다.")
    val name: String
) {
    fun toCommand() = SignUpMemberCommand(
        email = email,
        password = password,
        name = name
    )
}
