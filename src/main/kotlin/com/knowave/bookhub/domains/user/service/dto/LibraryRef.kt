package com.knowave.bookhub.domains.user.service.dto

import com.knowave.bookhub.domains.library.entity.Library
import java.util.UUID

data class LibraryRef(
    val id: UUID,
    val name: String,
) {
    companion object {
        fun from(library: Library) = LibraryRef(
            id = checkNotNull(library.id) { "영속화되지 않은 Library는 변환할 수 없습니다" },
            name = library.name,
        )
    }
}
