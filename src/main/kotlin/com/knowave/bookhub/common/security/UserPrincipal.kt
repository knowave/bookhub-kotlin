package com.knowave.bookhub.common.security

import com.knowave.bookhub.domains.user.entity.UserRole
import java.util.*

data class UserPrincipal(
    val id: UUID,
    val role: UserRole,
    val libraryId: UUID?
)
