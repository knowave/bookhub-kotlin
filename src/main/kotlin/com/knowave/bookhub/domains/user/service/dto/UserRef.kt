package com.knowave.bookhub.domains.user.service.dto

import com.knowave.bookhub.domains.user.entity.User
import java.util.UUID

data class UserRef(
    val id: UUID,
    val name: String,
) {
    companion object {
        fun from(user: User) = UserRef(
            id = checkNotNull(user.id) { "영속화되지 않은 User는 변환할 수 없습니다" },
            name = user.name,
        )
    }
}
