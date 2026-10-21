package com.knowave.bookhub.domains.loan.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import com.knowave.bookhub.domains.loan.service.dto.LoanResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class LoanExtensionResponse(
    @field:Schema(description = "대출 ID", example = "f7a8b9c0-1d2e-4f3a-8b5c-6d7e8f9a0b1c")
    val id: UUID,
    @field:Schema(description = "대출 상태 (LOANED, RETURNED, LOST)", example = "LOANED")
    val status: LoanStatus,
    @field:Schema(description = "반납 예정일. 연장하면 갱신된다", example = "2026-10-09")
    val dueDate: LocalDate,
    @field:Schema(description = "연장한 횟수", example = "1")
    val extensionCount: Int,
    @field:Schema(description = "남은 연장 가능 횟수", example = "1")
    val remainingExtensions: Int,
    @field:Schema(description = "연체 여부. 저장하지 않고 dueDate와 오늘을 비교해 계산한다", example = "false")
    @get:JsonProperty("isOverdue")
    val isOverdue: Boolean,
    @field:Schema(description = "연체일수. 연체가 아니면 0", example = "0")
    val overdueDays: Long,
) {
    companion object {
        fun from(result: LoanResult) = LoanExtensionResponse(
            id = result.id,
            status = result.status,
            dueDate = result.dueDate,
            extensionCount = result.extensionCount,
            remainingExtensions = result.remainingExtensions,
            isOverdue = result.isOverdue,
            overdueDays = result.overdueDays,
        )
    }
}
