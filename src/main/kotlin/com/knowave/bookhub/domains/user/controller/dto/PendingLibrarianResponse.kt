package com.knowave.bookhub.domains.user.controller.dto

import com.knowave.bookhub.domains.library.controller.dto.LibraryRefResponse
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.service.dto.UserResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class PendingLibrarianResponse(
    @field:Schema(description = "사서 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,

    @field:Schema(description = "사서 이름", example = "홍길동")
    val name: String,

    @field:Schema(description = "권한", example = "LIBRARIAN")
    val role: UserRole,

    @field:Schema(description = "계정 상태", example = "PENDING")
    val status: UserStatus,

    @field:Schema(description = "소속 지점")
    val library: LibraryRefResponse,
) {
    companion object {
        fun from(result: UserResult) = PendingLibrarianResponse(
            id = result.id,
            name = result.name,
            role = result.role,
            status = result.status,
            library = LibraryRefResponse.from(requireNotNull(result.library) { "사서는 반드시 소속 지점을 가져야 합니다 (M-6): ${result.id}" }),
        )
    }
}
