package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.loan.entity.LoanStatus

data class GetMyLoansCommand(
    val status: LoanStatus?,
    val caller: UserPrincipal,
)
