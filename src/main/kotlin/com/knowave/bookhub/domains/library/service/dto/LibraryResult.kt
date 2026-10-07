package com.knowave.bookhub.domains.library.service.dto

import com.knowave.bookhub.domains.library.entity.Library
import java.util.UUID
import java.time.Instant

data class LibraryResult(
    val id: UUID,
    val name: String,
    val address: String,
    val phone: String,
    val createdAt: Instant,
    val updateAt: Instant
) {
    companion object {
        fun from(library: Library) = LibraryResult(
            id = checkNotNull(library.id) { "영속화되지 않은 Library는 변화할 수 없습니다" },
            name = library.name,
            address = library.address,
            phone = library.phone,
            createdAt = library.createdAt,
            updateAt = library.updatedAt
        )
    }
}