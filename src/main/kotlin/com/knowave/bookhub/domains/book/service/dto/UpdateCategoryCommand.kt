package com.knowave.bookhub.domains.book.service.dto

import java.util.UUID

data class UpdateCategoryCommand(
    val categoryId: UUID,
    val name: String,
    val parentId: UUID?
)
