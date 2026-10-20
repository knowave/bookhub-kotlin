package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.library.service.dto.LibraryRef
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.service.dto.UserResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

data class SignUpResponse(
    @field:Schema(description = "가입된 이용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,

    @field:Schema(description = "로그인 아이디로 쓰는 이메일", example = "member@bookhub.io")
    val email: String,

    @field:Schema(description = "이용자 이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "권한", example = "MEMBER")
    val role: UserRole,

    @field:Schema(description = "계정 상태", example = "ACTIVE")
    val status: UserStatus,

    @field:Schema(description = "소속 지점. 사서만 값이 있고 일반 회원은 null")
    val library: LibraryRef?,

    @field:Schema(description = "가입 일시", example = "2026-10-20T09:00:00Z")
    val createdAt: Instant,
) {
    companion object {
        fun from(result: UserResult) = SignUpResponse(
            id = result.id,
            email = result.email,
            name = result.name,
            role = result.role,
            status = result.status,
            library = result.library,
            createdAt = result.createdAt
        )
    }
}

data class SignUpLibraryResponse(
    @field:Schema(description = "지점 ID", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    val id: UUID,

    @field:Schema(description = "지점명", example = "중앙도서관")
    val name: String,
) {
    companion object {
        fun from(ref: LibraryRef) = SignUpLibraryResponse(id = ref.id, name = ref.name)
    }
}
