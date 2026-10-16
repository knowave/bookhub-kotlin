package com.knowave.bookhub.domains.library.service.dto

import java.util.UUID

data class ChangeBookCopyStatusCommand(
    val bookCopyId: UUID,
    val action: BookCopyAction,
)

enum class BookCopyAction {
    DAMAGE_REPORT,
    REPAIR,
    LOST_REPORT,
}
