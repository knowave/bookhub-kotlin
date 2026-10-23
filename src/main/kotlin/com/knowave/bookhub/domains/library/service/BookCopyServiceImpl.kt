package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.domains.book.service.BookService
import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.exception.*
import com.knowave.bookhub.domains.library.repository.BookCopyRepository
import com.knowave.bookhub.domains.library.service.dto.BookCopyAction
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import com.knowave.bookhub.domains.library.service.dto.ChangeBookCopyStatusCommand
import com.knowave.bookhub.domains.library.service.dto.CreateBookCopyCommand
import com.knowave.bookhub.domains.loan.service.LoanService
import com.knowave.bookhub.domains.loan.service.dto.ClosedLoanRef
import org.springframework.context.annotation.Lazy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class BookCopyServiceImpl(
    private val bookCopyRepository: BookCopyRepository,
    private val bookService: BookService,
    private val libraryService: LibraryService,
    // B-5(복본 분실 → 대출 종료)와 UC-11(대출 생성 → 복본 필요)이 서로 반대 방향이라
    // LoanServiceImpl ↔ BookCopyServiceImpl 생성자 순환이 된다. @Lazy로 프록시를 받아
    // 첫 호출 시점에 해소한다. 분실 처리 경로에서만 쓰므로 기동 시 필요하지 않다.
    @Lazy private val loanService: LoanService
) : BookCopyService {

    @Transactional
    override fun createBookCopy(command: CreateBookCopyCommand): BookCopyResult {
        if (bookCopyRepository.existsByLibraryIdAndCallNumber(command.libraryId, command.callNumber)) {
            throw BookCopyCallNumberDuplicatedException(command.callNumber)
        }

        val book = bookService.getBookEntity(command.bookId)
        val library = libraryService.getLibraryEntity(command.libraryId)

        val bookCopy = BookCopy(
            callNumber = command.callNumber,
            book = book,
            library = library,
        )

        return BookCopyResult.from(bookCopyRepository.save(bookCopy), closedLoan = null)
    }

    @Transactional
    override fun changeStatus(command: ChangeBookCopyStatusCommand): BookCopyResult {
        val bookCopy = getBookCopyEntity(command.bookCopyId)
        var closedLoan: ClosedLoanRef? = null

        when (command.action) {
            BookCopyAction.DAMAGE_REPORT -> {
                if (bookCopy.status == BookCopyStatus.LOANED) {
                    throw BookCopyLoanedCannotDamageException(command.bookCopyId)
                }
                if (bookCopy.status != BookCopyStatus.AVAILABLE) {
                    throw BookCopyInvalidTransitionException(command.bookCopyId, bookCopy.status)
                }
                bookCopy.markAsDamaged()
            }

            BookCopyAction.REPAIR -> {
                if (bookCopy.status != BookCopyStatus.DAMAGED) {
                    throw BookCopyNotDamagedException(command.bookCopyId)
                }
                bookCopy.repair()
            }

            BookCopyAction.LOST_REPORT -> {
                if (bookCopy.status != BookCopyStatus.AVAILABLE && bookCopy.status != BookCopyStatus.LOANED) {
                    throw BookCopyInvalidTransitionException(command.bookCopyId, bookCopy.status)
                }
                // 대출 중이던 복본이면 그 대출도 함께 종료한다 (→ B-5).
                // Loan.loseCopy()가 복본까지 LOST로 바꾸므로 여기서 markAsLost()를 또 부르지 않는다.
                closedLoan = loanService.closeLoanForLostCopy(command.bookCopyId)
                if (closedLoan == null) bookCopy.markAsLost()
            }
        }

        return BookCopyResult.from(bookCopy, closedLoan)
    }

    override fun getBookCopyEntity(bookCopyId: UUID): BookCopy =
        bookCopyRepository.findByIdOrNull(bookCopyId)
            ?: throw BookCopyNotFoundException(bookCopyId)

    override fun getBookCopyEntityForLock(bookCopyId: UUID): BookCopy =
        bookCopyRepository.findWithLockById(bookCopyId)
            ?: throw BookCopyNotFoundException(bookCopyId)
}
