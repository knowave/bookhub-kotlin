package com.knowave.bookhub.domains.loan.service

import com.knowave.bookhub.domains.library.service.BookCopyService
import com.knowave.bookhub.domains.loan.entity.Loan
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import com.knowave.bookhub.domains.loan.exception.*
import com.knowave.bookhub.domains.loan.repository.LoanRepository
import com.knowave.bookhub.domains.loan.service.dto.*
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.exception.UserNotActiveException
import com.knowave.bookhub.domains.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional(readOnly = true)
class LoanServiceImpl(
    private val loanRepository: LoanRepository,
    private val userService: UserService,
    private val bookCopyService: BookCopyService,
) : LoanService {

    @Transactional
    override fun createLoan(command: CreateLoanCommand): LoanResult {
        val today = LocalDate.now()

        val bookCopy = bookCopyService.getBookCopyEntityForLock(command.bookCopyId)
        if (bookCopy.library.id != command.caller.libraryId) {
            throw LoanAccessDeniedLibraryException(command.bookCopyId)
        }

        val member = userService.getUserEntity(command.memberId)

        if (member.status != UserStatus.ACTIVE) {
            throw UserNotActiveException(command.memberId)
        }

        if (!bookCopy.isLoanable) {
            throw BookCopyNotAvailableException(command.bookCopyId)
        }

        if (loanRepository.countActiveLoans(command.memberId) >= MAX_ACTIVE_LOANS) {
            throw LoanLimitExceededException(command.memberId)
        }

        if (loanRepository.existsOverdue(command.memberId, today)) {
            throw LoanOverdueExistsException(command.memberId)
        }

        val loan = Loan.create(user = member, bookCopy = bookCopy, loanedAt = today)

        return LoanResult.from(loanRepository.save(loan), today)
    }

    @Transactional
    override fun returnLoan(command: ReturnLoanCommand): LoanResult {
        val today = LocalDate.now()

        val loan = getLoanEntity(command.loanId)
        if (loan.bookCopy.library.id != command.caller.libraryId) {
            throw LoanAccessDeniedLibraryException(
                checkNotNull(loan.bookCopy.id) { "영속화되지 않은 BookCopy는 대출에 연결될 수 없습니다" },
            )
        }

        if (loan.status != LoanStatus.LOANED) {
            throw LoanAlreadyClosedException(command.loanId)
        }

        loan.returnBook(today)

        return LoanResult.from(loan, today)
    }

    @Transactional
    override fun extendLoan(command: ExtendLoanCommand): LoanResult {
        val today = LocalDate.now()
        val loan = getLoanEntity(command.loanId)

        when (command.caller.role) {
            UserRole.MEMBER -> {
                if (loan.user.id != command.caller.id) {
                    throw LoanNotOwnedException(command.loanId)
                }
            }

            UserRole.LIBRARIAN -> {
                if (loan.bookCopy.library.id != command.caller.libraryId) {
                    throw LoanAccessDeniedLibraryException(
                        checkNotNull(loan.bookCopy.id) { "영속화되지 않은 BookCopy는 대출에 연결될 수 없습니다" },
                    )
                }
            }

            UserRole.ADMIN -> throw LoanAccessDeniedRoleException()
        }

        if (loan.status != LoanStatus.LOANED) {
            throw LoanAlreadyClosedException(command.loanId)
        }

        if (loan.extensionCount >= Loan.MAX_EXTENSION_COUNT) {
            throw LoanExtensionLimitExceededException(command.loanId)
        }

        if (loan.isOverdue(today)) {
            throw LoanOverdueCannotExtendException(command.loanId)
        }

        loan.extend(today)

        return LoanResult.from(loan, today)
    }

    override fun getMyLoans(command: GetMyLoansCommand, pageable: Pageable): Page<LoanResult> {
        val today = LocalDate.now()

        return loanRepository.findAllByUserIdAndOptionalStatus(command.caller.id, command.status, pageable)
            .map { loan -> LoanResult.from(loan, today) }
    }

    override fun getLibraryLoans(command: GetLibraryLoansCommand, pageable: Pageable): Page<LoanResult> {
        val today = LocalDate.now()
        val libraryId = command.caller.libraryId ?: throw LoanAccessDeniedRoleException()

        return loanRepository.findAllByBookCopyLibraryId(libraryId, pageable)
            .map { loan -> LoanResult.from(loan, today) }
    }

    @Transactional
    override fun closeLoanForLostCopy(bookCopyId: UUID): ClosedLoanRef? {
        val loan = loanRepository.findLoanedByBookCopyId(bookCopyId) ?: return null

        loan.loseCopy(LocalDate.now())

        return ClosedLoanRef.from(loan)
    }

    private fun getLoanEntity(loanId: UUID): Loan =
        loanRepository.findByIdOrNull(loanId)
            ?: throw LoanNotFoundException(loanId)

    // L-2의 동시 대출 한도이며 Loan 엔티티를 수정하지 않기 위해 서비스에 둔다
    private companion object {
        const val MAX_ACTIVE_LOANS = 3L
    }
}
