package com.knowave.bookhub.domains.loan.service

import com.knowave.bookhub.domains.loan.service.dto.ClosedLoanRef
import com.knowave.bookhub.domains.loan.service.dto.CreateLoanCommand
import com.knowave.bookhub.domains.loan.service.dto.ExtendLoanCommand
import com.knowave.bookhub.domains.loan.service.dto.GetLibraryLoansCommand
import com.knowave.bookhub.domains.loan.service.dto.GetMyLoansCommand
import com.knowave.bookhub.domains.loan.service.dto.LoanResult
import com.knowave.bookhub.domains.loan.service.dto.ReturnLoanCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface LoanService {

    /**
     * 대출을 처리한다. (UC-11)
     * - 복본의 지점이 사서의 소속 지점과 다르면 LoanAccessDeniedLibraryException (→ P-8)
     * - 회원이 ACTIVE가 아니면 UserNotActiveException (→ L-13)
     * - 복본이 AVAILABLE이 아니면 BookCopyNotAvailableException (→ L-10)
     * - 진행 중인 대출이 이미 3건이면 LoanLimitExceededException (→ L-2)
     * - 연체 중인 대출이 있으면 LoanOverdueExistsException (→ L-3)
     * - 반납 예정일은 대출일 + 14일 (→ L-1)
     * 회원이 없으면 UserNotFoundException, 복본이 없으면 BookCopyNotFoundException.
     */
    fun createLoan(command: CreateLoanCommand): LoanResult

    /**
     * 반납을 처리한다. (UC-12)
     * - 복본의 지점이 사서의 소속 지점과 다르면 LoanAccessDeniedLibraryException (→ P-8)
     * - 대출이 LOANED가 아니면 LoanAlreadyClosedException (→ L-12)
     * - 복본은 AVAILABLE로 돌아간다
     * 대출이 없으면 LoanNotFoundException.
     */
    fun returnLoan(command: ReturnLoanCommand): LoanResult

    /**
     * 대출을 연장한다. (UC-13 / UC-14)
     * - 호출자가 MEMBER면 본인 대출인지 검사한다. 아니면 LoanNotOwnedException (→ L-8)
     * - 호출자가 LIBRARIAN이면 복본의 지점을 검사한다. 다르면 LoanAccessDeniedLibraryException (→ P-8)
     * - 호출자가 ADMIN이면 LoanAccessDeniedRoleException (→ P-1)
     * - 대출이 LOANED가 아니면 LoanAlreadyClosedException (→ L-12)
     * - 이미 2회 연장했으면 LoanExtensionLimitExceededException (→ L-4)
     * - 연체 중이면 LoanOverdueCannotExtendException (→ L-5)
     * dueDate가 7일 늘고 extensionCount가 1 오른다. 새 대출 행은 생기지 않는다.
     */
    fun extendLoan(command: ExtendLoanCommand): LoanResult

    /**
     * 본인 대출 이력을 조회한다. (UC-15)
     * - 대상 회원은 호출자 자신이다. 다른 회원의 이력은 조회할 수 없다
     * - status를 주면 해당 상태만, 주지 않으면 전체를 돌려준다
     */
    fun getMyLoans(command: GetMyLoansCommand, pageable: Pageable): Page<LoanResult>

    /**
     * 사서의 소속 지점 대출 현황을 조회한다. (UC-16)
     * - 조회 범위는 호출자의 소속 지점으로 서버가 정한다. 클라이언트가 지점을 지정할 수 없다 (→ P-8)
     * - 소속 지점이 없으면 LoanAccessDeniedRoleException
     */
    fun getLibraryLoans(command: GetLibraryLoansCommand, pageable: Pageable): Page<LoanResult>

    /**
     * 복본 분실 처리(UC-10-2)에 연계되어 진행 중인 대출을 종료한다 (→ B-5).
     * 독립적인 유스케이스가 아니며 BookCopyService에서만 호출한다.
     * - 해당 복본에 LOANED 대출이 없으면 null을 돌려준다
     * - Loan.loseCopy()가 복본까지 LOST로 바꾸므로 호출 측에서 복본 상태를 또 바꾸지 않는다
     * - 전이 가능 여부(B-1, B-3) 검사는 복본의 책임이므로 호출 측이 먼저 한다
     */
    fun closeLoanForLostCopy(bookCopyId: UUID): ClosedLoanRef?
}
