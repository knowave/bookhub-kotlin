package com.knowave.bookhub.domains.book.service

import com.knowave.bookhub.domains.book.entity.Category
import com.knowave.bookhub.domains.book.exception.CategoryCircularReferenceException
import com.knowave.bookhub.domains.book.exception.CategoryNotFoundException
import com.knowave.bookhub.domains.book.repository.CategoryRepository
import com.knowave.bookhub.domains.book.service.dto.CategoryResult
import com.knowave.bookhub.domains.book.service.dto.CreateCategoryCommand
import com.knowave.bookhub.domains.book.service.dto.UpdateCategoryCommand
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
) : CategoryService {

    @Transactional
    override fun createCategory(command: CreateCategoryCommand): CategoryResult {
        val parent = command.parentId?.let { getCategoryEntity(it) }

        val category = Category(name = command.name, parent = parent)

        return CategoryResult.from(categoryRepository.save(category))
    }

    @Transactional
    override fun updateCategory(command: UpdateCategoryCommand): CategoryResult {
        val category = getCategoryEntity(command.categoryId)
        val newParent = command.parentId?.let { getCategoryEntity(it) }

        if (newParent != null && newParent.isSelfOrDescendantOf(category)) {
            throw CategoryCircularReferenceException(command.categoryId, command.parentId)
        }

        category.changeName(command.name)
        category.changeParent(newParent)

        return CategoryResult.from(category)
    }

    override fun getCategories(): List<CategoryResult> =
        categoryRepository.findAllByParentIsNull()
            .map { CategoryResult.from(it) }

    override fun getCategoryEntity(categoryId: UUID): Category =
        categoryRepository.findByIdOrNull(categoryId)
            ?: throw CategoryNotFoundException(categoryId)
}
