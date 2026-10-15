package com.knowave.bookhub.domains.book.service

import com.knowave.bookhub.domains.book.entity.Book
import com.knowave.bookhub.domains.book.exception.BookIsbnDuplicatedException
import com.knowave.bookhub.domains.book.exception.BookNotFoundException
import com.knowave.bookhub.domains.book.repository.BookRepository
import com.knowave.bookhub.domains.book.service.dto.BookResult
import com.knowave.bookhub.domains.book.service.dto.CreateBookCommand
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val categoryService: CategoryService
) : BookService {

    @Transactional
    override fun createBook(command: CreateBookCommand): BookResult {
        if (bookRepository.existsByIsbn(command.isbn)) {
            throw BookIsbnDuplicatedException(command.isbn)
        }

        val category = categoryService.getCategoryEntity(command.categoryId)

        val book = Book(
            isbn = command.isbn,
            title = command.title,
            author = command.author,
            publisher = command.publisher,
            publishedAt = command.publishedAt,
            description = command.description,
            category = category,
        )

        // 생성 직후에는 복본이 없다. 실제 집계는 도서 상세(UC-07)에서 구현한다
        return BookResult.from(bookRepository.save(book), totalCopies = 0)
    }

    override fun getBookEntity(bookId: UUID): Book =
        bookRepository.findByIdOrNull(bookId)
            ?: throw BookNotFoundException(bookId)
}
