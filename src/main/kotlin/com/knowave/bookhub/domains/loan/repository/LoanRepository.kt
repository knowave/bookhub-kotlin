package com.knowave.bookhub.domains.loan.repository

import com.knowave.bookhub.domains.loan.entity.Loan
import com.knowave.bookhub.domains.loan.entity.LoanStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.*

interface LoanRepository : JpaRepository<Loan, UUID> {

    // 특정 회원의 현재 대출이 몇 권인지 확인.
    @Query("select count(l) from Loan l where l.user.id = :userId and l.status = 'LOANED'")
    fun countActiveLoans(@Param("userId") userId: UUID): Long


    // 연체 여부 확인
    @Query("""
    select count(l) > 0 from Loan l
    where l.user.id = :userId
      and l.status = 'LOANED'
      and l.dueDate < :baseDate
""")
    fun existsOverdue(
        @Param("userId") userId: UUID,
        @Param("baseDate") baseDate: LocalDate
    ): Boolean

    @Query("""
    select l from Loan l
    where l.user.id = :userId
      and (:status is null or l.status = :status)
""")
    @EntityGraph(attributePaths = ["user", "bookCopy", "bookCopy.book", "bookCopy.library"])
    fun findAllByUserIdAndOptionalStatus(userId: UUID, status: LoanStatus?, pageable: Pageable): Page<Loan>

    @EntityGraph(attributePaths = ["user", "bookCopy", "bookCopy.book", "bookCopy.library"])
    fun findAllByBookCopyLibraryId(libraryId: UUID, pageable: Pageable): Page<Loan>

    // 복본 분실 시 함께 종료할 대출을 찾는다 (→ B-5). B-1에 따라 한 복본의 LOANED 대출은 최대 1건이다.
    @Query("""
    select l from Loan l
    where l.bookCopy.id = :bookCopyId
      and l.status = 'LOANED'
""")
    fun findLoanedByBookCopyId(@Param("bookCopyId") bookCopyId: UUID): Loan?
}