package com.knowave.bookhub.domains.book.service.dto

import java.util.UUID

data class CreateCategoryCommand(
    val name: String,
    val parentId: UUID?
)