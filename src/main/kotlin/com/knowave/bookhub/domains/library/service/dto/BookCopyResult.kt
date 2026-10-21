package com.knowave.bookhub.domains.library.service.dto

import com.knowave.bookhub.domains.book.service.dto.BookRef
import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.loan.service.dto.ClosedLoanRef
import java.util.UUID

data class BookCopyResult(
    val id: UUID,
    val callNumber: String,
    val status: BookCopyStatus,
    val book: BookRef,
    val library: LibraryRef,
    // 분실 처리로 함께 종료된 대출 (→ B-5). 그 외 경우는 항상 null
    val closedLoan: ClosedLoanRef?,
) {
    companion object {
        fun from(bookCopy: BookCopy, closedLoan: ClosedLoanRef?) = BookCopyResult(
            id = checkNotNull(bookCopy.id) { "영속화되지 않은 BookCopy는 변환할 수 없습니다" },
            callNumber = bookCopy.callNumber,
            status = bookCopy.status,
            book = BookRef.from(bookCopy.book),
            library = LibraryRef.from(bookCopy.library),
            closedLoan = closedLoan,
        )
    }
}
