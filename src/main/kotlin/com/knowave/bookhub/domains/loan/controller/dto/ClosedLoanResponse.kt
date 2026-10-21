package com.knowave.bookhub.domains.loan.controller.dto

import com.knowave.bookhub.domains.loan.entity.LoanStatus
import com.knowave.bookhub.domains.loan.service.dto.ClosedLoanRef
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class ClosedLoanResponse(
    @field:Schema(description = "대출 ID", example = "f7a8b9c0-1d2e-4f3a-8b5c-6d7e8f9a0b1c")
    val id: UUID,

    @field:Schema(description = "종료된 대출 상태 (LOANED, RETURNED, LOST)", example = "LOST")
    val status: LoanStatus,

    @field:Schema(description = "대출 종료일. 분실 판정일이 들어간다", example = "2026-09-18")
    val closedAt: LocalDate,
) {
    companion object {
        fun from(ref: ClosedLoanRef) = ClosedLoanResponse(
            id = ref.id,
            status = ref.status,
            closedAt = ref.closedAt,
        )
    }
}
