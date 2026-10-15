package com.knowave.bookhub.domains.book.controller

import com.knowave.bookhub.domains.book.controller.dto.CategoryResponse
import com.knowave.bookhub.domains.book.controller.dto.CreateCategoryRequest
import com.knowave.bookhub.domains.book.controller.dto.UpdateCategoryRequest
import com.knowave.bookhub.domains.book.service.CategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    // TODO: ADMIN 권한 검증 필요 (→ P-6). JWT 도입 시 추가
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(@RequestBody @Valid request: CreateCategoryRequest): CategoryResponse =
        CategoryResponse.from(categoryService.createCategory(request.toCommand()))

    // TODO: ADMIN 권한 검증 필요 (→ P-6). JWT 도입 시 추가
    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateCategoryRequest
    ): CategoryResponse =
        CategoryResponse.from(categoryService.updateCategory(request.toCommand(categoryId = id)))

    // TODO: 인증 필요 (MEMBER). JWT 도입 시 추가
    @GetMapping
    fun getManyCategory(): List<CategoryResponse> =
        categoryService.getCategories().map { CategoryResponse.from(it) }
}
