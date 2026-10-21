package com.knowave.bookhub.domains.loan.controller.dto

import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.loan.service.dto.CreateLoanCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateLoanRequest(
    @field:Schema(description = "대출받을 회원 ID. 사서 대행 처리이므로 body로 받는다", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val memberId: UUID,

    @field:Schema(description = "대출할 복본 ID", example = "d5e6f7a8-1b2c-4d3e-9f40-5a6b7c8d9e0f")
    val bookCopyId: UUID,
) {

    fun toCommand(caller: UserPrincipal) = CreateLoanCommand(
        memberId = memberId,
        bookCopyId = bookCopyId,
        caller = caller,
    )
}
