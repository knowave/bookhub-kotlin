package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.common.security.UserPrincipal
import java.util.UUID

data class ExtendLoanCommand(
    val loanId: UUID,
    val caller: UserPrincipal,
)
