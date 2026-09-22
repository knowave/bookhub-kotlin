package com.knowave.bookhub.domains.user.service.dto

import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import java.time.Instant
import java.util.UUID

data class UserResult(
    val id: UUID,
    val email: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val library: LibraryRef?,
    val createdAt: Instant,
) {
    companion object {
        fun from(user: User) = UserResult(
            id = checkNotNull(user.id) { "영속화되지 않은 User는 변환할 수 없습니다" },
            email = user.email,
            name = user.name,
            role = user.role,
            status = user.status,
            library = user.library?.let { LibraryRef.from(it) },
            createdAt = user.createdAt,
        )
    }
}
