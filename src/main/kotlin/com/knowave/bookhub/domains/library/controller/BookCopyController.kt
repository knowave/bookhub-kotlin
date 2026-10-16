package com.knowave.bookhub.domains.library.controller

import com.knowave.bookhub.domains.library.controller.dto.BookCopyLostResponse
import com.knowave.bookhub.domains.library.controller.dto.BookCopyResponse
import com.knowave.bookhub.domains.library.controller.dto.BookCopyStatusResponse
import com.knowave.bookhub.domains.library.controller.dto.CreateBookCopyRequest
import com.knowave.bookhub.domains.library.service.BookCopyService
import com.knowave.bookhub.domains.library.service.dto.BookCopyAction
import com.knowave.bookhub.domains.library.service.dto.ChangeBookCopyStatusCommand
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/book-copies")
class BookCopyController(
    private val bookCopyService: BookCopyService
) {

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-8). JWT 도입 시 추가
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBookCopy(@RequestBody @Valid request: CreateBookCopyRequest): BookCopyResponse =
        BookCopyResponse.from(bookCopyService.createBookCopy(request.toCommand()))

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-8). JWT 도입 시 추가
    @PostMapping("/{id}/damage-report")
    fun reportDamage(@PathVariable id: UUID): BookCopyStatusResponse =
        BookCopyStatusResponse.from(
            bookCopyService.changeStatus(ChangeBookCopyStatusCommand(bookCopyId = id, action = BookCopyAction.DAMAGE_REPORT))
        )

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-8). JWT 도입 시 추가
    @PostMapping("/{id}/repair")
    fun repair(@PathVariable id: UUID): BookCopyStatusResponse =
        BookCopyStatusResponse.from(
            bookCopyService.changeStatus(ChangeBookCopyStatusCommand(bookCopyId = id, action = BookCopyAction.REPAIR))
        )

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-8). JWT 도입 시 추가
    @PostMapping("/{id}/lost-report")
    fun reportLost(@PathVariable id: UUID): BookCopyLostResponse =
        BookCopyLostResponse.from(
            bookCopyService.changeStatus(ChangeBookCopyStatusCommand(bookCopyId = id, action = BookCopyAction.LOST_REPORT))
        )
}
