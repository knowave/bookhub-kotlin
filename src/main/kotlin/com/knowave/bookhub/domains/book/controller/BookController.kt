package com.knowave.bookhub.domains.book.controller

import com.knowave.bookhub.domains.book.controller.dto.BookResponse
import com.knowave.bookhub.domains.book.controller.dto.CreateBookRequest
import com.knowave.bookhub.domains.book.service.BookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookService: BookService
) {

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-3). 소속 지점은 검사하지 않는다 (→ P-9). JWT 도입 시 추가
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody @Valid request: CreateBookRequest): BookResponse =
        BookResponse.from(bookService.createBook(request.toCommand()))
}
