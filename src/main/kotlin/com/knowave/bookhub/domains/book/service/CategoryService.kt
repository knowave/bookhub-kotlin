package com.knowave.bookhub.domains.book.service

import com.knowave.bookhub.domains.book.entity.Category
import com.knowave.bookhub.domains.book.service.dto.CategoryResult
import com.knowave.bookhub.domains.book.service.dto.CreateCategoryCommand
import com.knowave.bookhub.domains.book.service.dto.UpdateCategoryCommand
import java.util.UUID

interface CategoryService {

    fun createCategory(command: CreateCategoryCommand): CategoryResult

    fun updateCategory(command: UpdateCategoryCommand): CategoryResult

    fun getCategories(): List<CategoryResult>

    fun getCategoryEntity(categoryId: UUID): Category
}