package com.knowave.bookhub.domains.user.controller.dto

import com.knowave.bookhub.domains.library.controller.dto.LibraryResponse
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.service.dto.UserResult
import java.util.UUID

data class ApprovedLibrarianResponse(
    val id: UUID,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val library: LibraryResponse,
) {
    companion object {
        fun from(result: UserResult)= ApprovedLibrarianResponse(
            id = result.id,
            name = result.name,
            role = result.role,
            status = result.status,
            library = LibraryResponse.from(requireNotNull(result.library) { "사서는 반드시 소속 지점을 가져야 합니다 (M-6): ${result.id}" }),
        )
    }
}
