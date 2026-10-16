package com.knowave.bookhub.domains.library.service.dto

import java.util.UUID

data class CreateBookCopyCommand(
    val bookId: UUID,
    val libraryId: UUID,
    val callNumber: String,
)
