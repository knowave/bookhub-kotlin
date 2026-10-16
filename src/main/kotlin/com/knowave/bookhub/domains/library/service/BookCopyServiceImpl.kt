package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.domains.book.service.BookService
import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.library.entity.BookCopyStatus
import com.knowave.bookhub.domains.library.exception.BookCopyCallNumberDuplicatedException
import com.knowave.bookhub.domains.library.exception.BookCopyInvalidTransitionException
import com.knowave.bookhub.domains.library.exception.BookCopyLoanedCannotDamageException
import com.knowave.bookhub.domains.library.exception.BookCopyNotDamagedException
import com.knowave.bookhub.domains.library.exception.BookCopyNotFoundException
import com.knowave.bookhub.domains.library.repository.BookCopyRepository
import com.knowave.bookhub.domains.library.service.dto.BookCopyAction
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import com.knowave.bookhub.domains.library.service.dto.ChangeBookCopyStatusCommand
import com.knowave.bookhub.domains.library.service.dto.CreateBookCopyCommand
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class BookCopyServiceImpl(
    private val bookCopyRepository: BookCopyRepository,
    private val bookService: BookService,
    private val libraryService: LibraryService
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

        return BookCopyResult.from(bookCopyRepository.save(bookCopy))
    }

    @Transactional
    override fun changeStatus(command: ChangeBookCopyStatusCommand): BookCopyResult {
        val bookCopy = getBookCopyEntity(command.bookCopyId)

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
                // TODO: 대출 중 복본 분실 시 해당 대출도 LOST로 종료해야 한다 (→ B-5). Loan 도메인 구현 시 추가
                bookCopy.markAsLost()
            }
        }

        return BookCopyResult.from(bookCopy)
    }

    override fun getBookCopyEntity(bookCopyId: UUID): BookCopy =
        bookCopyRepository.findByIdOrNull(bookCopyId)
            ?: throw BookCopyNotFoundException(bookCopyId)
}
