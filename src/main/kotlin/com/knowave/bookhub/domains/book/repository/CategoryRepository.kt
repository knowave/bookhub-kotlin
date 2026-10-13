package com.knowave.bookhub.domains.book.repository

import com.knowave.bookhub.domains.book.entity.Category
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {

    @EntityGraph(attributePaths = ["_children"])
    fun findAllByParentIsNull(): List<Category>
}
