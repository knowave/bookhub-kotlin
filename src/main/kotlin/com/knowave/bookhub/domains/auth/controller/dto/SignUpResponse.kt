package com.knowave.bookhub.domains.auth.controller.dto

import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.service.dto.LibraryRef
import com.knowave.bookhub.domains.user.service.dto.UserResult
import java.time.Instant
import java.util.UUID

data class SignUpResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val library: LibraryRef?,
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
    val id: UUID,
    val name: String,
) {
    companion object {
        fun from(ref: LibraryRef) = SignUpLibraryResponse(id = ref.id, name = ref.name)
    }
}