package com.knowave.bookhub.domains.loan.controller.dto

import com.knowave.bookhub.domains.book.controller.dto.BookRefResponse
import com.knowave.bookhub.domains.library.service.dto.BookCopyRef
import io.swagger.v3.oas.annotations.media.Schema

data class LibraryLoanBookCopyResponse(
    @field:Schema(description = "청구기호. 지점 내에서 실물을 식별한다", example = "833.6-무라카-3")
    val callNumber: String,

    @field:Schema(description = "복본의 원본 도서 참조 객체")
    val book: BookRefResponse,
) {
    companion object {
        fun from(ref: BookCopyRef) = LibraryLoanBookCopyResponse(
            callNumber = ref.callNumber,
            book = BookRefResponse.from(ref.book),
        )
    }
}
