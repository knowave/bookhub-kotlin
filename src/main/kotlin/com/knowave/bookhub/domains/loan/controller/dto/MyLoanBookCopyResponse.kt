package com.knowave.bookhub.domains.loan.controller.dto

import com.knowave.bookhub.domains.book.controller.dto.BookRefResponse
import com.knowave.bookhub.domains.library.controller.dto.LibraryRefResponse
import com.knowave.bookhub.domains.library.service.dto.BookCopyRef
import io.swagger.v3.oas.annotations.media.Schema

data class MyLoanBookCopyResponse(
    @field:Schema(description = "청구기호. 지점 내에서 실물을 식별한다", example = "833.6-무라카-3")
    val callNumber: String,

    @field:Schema(description = "복본의 원본 도서 참조 객체")
    val book: BookRefResponse,

    @field:Schema(description = "복본이 비치된 지점 참조 객체")
    val library: LibraryRefResponse,
) {
    companion object {
        fun from(ref: BookCopyRef) = MyLoanBookCopyResponse(
            callNumber = ref.callNumber,
            book = BookRefResponse.from(ref.book),
            library = LibraryRefResponse.from(ref.library),
        )
    }
}
