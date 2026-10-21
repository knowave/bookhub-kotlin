package com.knowave.bookhub.domains.loan.controller

import com.knowave.bookhub.common.dto.PageResponse
import com.knowave.bookhub.common.security.CurrentUser
import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.loan.controller.dto.CreateLoanRequest
import com.knowave.bookhub.domains.loan.controller.dto.LibraryLoanResponse
import com.knowave.bookhub.domains.loan.controller.dto.LoanExtensionResponse
import com.knowave.bookhub.domains.loan.controller.dto.LoanResponse
import com.knowave.bookhub.domains.loan.controller.dto.LoanReturnResponse
import com.knowave.bookhub.domains.loan.controller.dto.MyLoanResponse
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import com.knowave.bookhub.domains.loan.service.LoanService
import com.knowave.bookhub.domains.loan.service.dto.ExtendLoanCommand
import com.knowave.bookhub.domains.loan.service.dto.GetLibraryLoansCommand
import com.knowave.bookhub.domains.loan.service.dto.GetMyLoansCommand
import com.knowave.bookhub.domains.loan.service.dto.ReturnLoanCommand
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/loans")
class LoanController(
    private val loanService: LoanService
) {

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-3, L-6). 소속 지점 검사(P-8)는 서비스에 있다
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLoan(
        @RequestBody @Valid request: CreateLoanRequest,
        @CurrentUser caller: UserPrincipal,
    ): LoanResponse =
        LoanResponse.from(loanService.createLoan(request.toCommand(caller)))

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-3, L-6). 소속 지점 검사(P-8)는 서비스에 있다
    @PostMapping("/{id}/return")
    fun returnLoan(
        @PathVariable id: UUID,
        @CurrentUser caller: UserPrincipal,
    ): LoanReturnResponse =
        LoanReturnResponse.from(loanService.returnLoan(ReturnLoanCommand(loanId = id, caller = caller)))

    // 역할별 분기는 서비스가 한다 — MEMBER는 소유권(L-8), LIBRARIAN은 소속 지점(P-8), ADMIN은 거부(P-1)
    @PostMapping("/{id}/extensions")
    fun extendLoan(
        @PathVariable id: UUID,
        @CurrentUser caller: UserPrincipal,
    ): LoanExtensionResponse =
        LoanExtensionResponse.from(loanService.extendLoan(ExtendLoanCommand(loanId = id, caller = caller)))

    @GetMapping("/me")
    fun getMyLoans(
        @RequestParam(required = false) status: LoanStatus?,
        @CurrentUser caller: UserPrincipal,
        @PageableDefault(size = 20, sort = ["loanedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): PageResponse<MyLoanResponse> =
        PageResponse.from(loanService.getMyLoans(GetMyLoansCommand(status = status, caller = caller), pageable)) {
            MyLoanResponse.from(it)
        }

    // TODO: LIBRARIAN 권한 검증 필요 (→ P-3). 조회 범위는 서비스가 호출자의 소속 지점으로 정한다 (→ P-8)
    @GetMapping
    fun getLibraryLoans(
        @CurrentUser caller: UserPrincipal,
        @PageableDefault(size = 20, sort = ["dueDate"]) pageable: Pageable,
    ): PageResponse<LibraryLoanResponse> =
        PageResponse.from(loanService.getLibraryLoans(GetLibraryLoansCommand(caller = caller), pageable)) {
            LibraryLoanResponse.from(it)
        }
}
