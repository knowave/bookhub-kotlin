package com.knowave.bookhub.domains.book.repository

import com.knowave.bookhub.domains.book.entity.Book
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BookRepository : JpaRepository<Book, UUID> {

    fun existsByIsbn(isbn: String): Boolean
}
