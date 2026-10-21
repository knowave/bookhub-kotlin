package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.domains.loan.entity.Loan
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import java.time.LocalDate
import java.util.UUID

data class ClosedLoanRef(
    val id: UUID,
    val status: LoanStatus,
    val closedAt: LocalDate,
) {
    companion object {
        fun from(loan: Loan) = ClosedLoanRef(
            id = checkNotNull(loan.id) { "영속화되지 않은 Loan은 변환할 수 없습니다" },
            status = loan.status,
            closedAt = checkNotNull(loan.closedAt) { "종료되지 않은 Loan은 종료 참조로 변환할 수 없습니다" },
        )
    }
}
