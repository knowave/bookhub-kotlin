package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.library.service.dto.BookCopyResult
import com.knowave.bookhub.domains.library.service.dto.ChangeBookCopyStatusCommand
import com.knowave.bookhub.domains.library.service.dto.CreateBookCopyCommand
import java.util.UUID

interface BookCopyService {

    /**
     * 복본을 등록한다. (UC-09)
     * 같은 지점에 청구기호가 이미 있으면 BookCopyCallNumberDuplicatedException,
     * 도서가 없으면 BookNotFoundException, 지점이 없으면 LibraryNotFoundException.
     */
    fun createBookCopy(command: CreateBookCopyCommand): BookCopyResult

    /**
     * 복본 상태를 변경한다. (UC-10 / 10-1 / 10-2)
     * - DAMAGE_REPORT(파손 처리): AVAILABLE에서만 가능. LOANED면 BookCopyLoanedCannotDamageException,
     *   그 외 상태면 BookCopyInvalidTransitionException.
     * - REPAIR(수선 완료): DAMAGED에서만 가능. 아니면 BookCopyNotDamagedException.
     * - LOST_REPORT(분실 처리): AVAILABLE 또는 LOANED에서 가능. 그 외 상태면 BookCopyInvalidTransitionException.
     * 복본이 없으면 BookCopyNotFoundException.
     */
    fun changeStatus(command: ChangeBookCopyStatusCommand): BookCopyResult

    /**
     * 대출 생성(UC-11)에서 연관관계를 맺기 위해 엔티티를 반환한다.
     * 존재하지 않으면 BookCopyNotFoundException.
     */
    fun getBookCopyEntity(bookCopyId: UUID): BookCopy
}
