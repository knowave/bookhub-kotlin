package com.knowave.bookhub.domains.book.service

import com.knowave.bookhub.domains.book.entity.Book
import com.knowave.bookhub.domains.book.service.dto.BookResult
import com.knowave.bookhub.domains.book.service.dto.CreateBookCommand
import java.util.UUID

interface BookService {

    /**
     * 도서를 등록한다. (UC-08)
     * ISBN이 이미 존재하면 BookIsbnDuplicatedException, 분류가 없으면 CategoryNotFoundException.
     */
    fun createBook(command: CreateBookCommand): BookResult

    /**
     * 복본 등록(UC-09)에서 연관관계를 맺기 위해 엔티티를 반환한다.
     * 존재하지 않으면 BookNotFoundException.
     */
    fun getBookEntity(bookId: UUID): Book
}
