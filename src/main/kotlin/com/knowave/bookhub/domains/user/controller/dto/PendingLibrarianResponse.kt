package com.knowave.bookhub.domains.user.controller.dto

import com.knowave.bookhub.domains.library.controller.dto.LibraryRefResponse
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.service.dto.UserResult
import java.util.UUID

data class PendingLibrarianResponse(
    val id: UUID,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
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
