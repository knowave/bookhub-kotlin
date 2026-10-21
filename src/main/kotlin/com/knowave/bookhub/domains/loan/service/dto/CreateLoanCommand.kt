package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.common.security.UserPrincipal
import java.util.UUID

data class CreateLoanCommand(
    val memberId: UUID,
    val bookCopyId: UUID,
    val caller: UserPrincipal,
)
