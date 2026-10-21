package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.domains.library.service.dto.BookCopyRef
import com.knowave.bookhub.domains.loan.entity.Loan
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import com.knowave.bookhub.domains.user.service.dto.UserRef
import java.time.LocalDate
import java.util.UUID

data class LoanResult(
    val id: UUID,
    val status: LoanStatus,
    val loanedAt: LocalDate,
    val dueDate: LocalDate,
    val closedAt: LocalDate?,
    val extensionCount: Int,
    // L-4의 남은 연장 가능 횟수
    val remainingExtensions: Int,
    val isOverdue: Boolean,
    val overdueDays: Long,
    val user: UserRef,
    val bookCopy: BookCopyRef,
) {
    companion object {
        fun from(loan: Loan, baseDate: LocalDate) = LoanResult(
            id = checkNotNull(loan.id) { "영속화되지 않은 Loan은 변환할 수 없습니다" },
            status = loan.status,
            loanedAt = loan.loanedAt,
            dueDate = loan.dueDate,
            closedAt = loan.closedAt,
            extensionCount = loan.extensionCount,
            remainingExtensions = Loan.MAX_EXTENSION_COUNT - loan.extensionCount,
            isOverdue = loan.isOverdue(baseDate),
            overdueDays = loan.overdueDays(baseDate),
            user = UserRef.from(loan.user),
            bookCopy = BookCopyRef.from(loan.bookCopy),
        )
    }
}
