package com.knowave.bookhub.domains.loan.service.dto

import com.knowave.bookhub.common.security.UserPrincipal

data class GetLibraryLoansCommand(
    val caller: UserPrincipal,
)
