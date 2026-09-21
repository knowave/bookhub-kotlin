package com.knowave.bookhub.domains.loan.entity

import com.knowave.bookhub.common.entity.BaseEntity
import com.knowave.bookhub.domains.library.entity.BookCopy
import com.knowave.bookhub.domains.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "loans",
    comment = "대출 건. 대출부터 반납까지가 하나의 행으로 유지된다",
    indexes = [
        Index(name = "idx_loans_user_status", columnList = "user_id, status"),
        Index(name = "idx_loans_overdue", columnList = "status, due_date"),
    ],
)
class Loan private constructor(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, comment = "대출한 이용자")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false, comment = "대출된 복본")
    val bookCopy: BookCopy,

    @Column(nullable = false, comment = "대출 시작일")
    val loanedAt: LocalDate,

    @Column(name = "due_date", nullable = false, comment = "반납 예정일. 연장 시 갱신된다")
    var dueDate: LocalDate,

    ) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "대출 상태")
    var status: LoanStatus = LoanStatus.LOANED
        protected set

    @Column(comment = "대출 종료일. 반납이든 분실이든 종료 시점을 기록한다. 종료 전에는 null")
    var closedAt: LocalDate? = null
        protected set

    @Column(nullable = false, comment = "연장 횟수")
    var extensionCount: Int = 0
        protected set

    /** 연체일수. 연체가 아니면 0 */
    fun overdueDays(baseDate: LocalDate = LocalDate.now()): Long =
        if (status == LoanStatus.LOANED && baseDate > dueDate) {
            ChronoUnit.DAYS.between(dueDate, baseDate)
        } else 0

    fun isOverdue(baseDate: LocalDate = LocalDate.now()): Boolean = overdueDays(baseDate) > 0

    fun returnBook(returnDate: LocalDate = LocalDate.now()) {
        check(status == LoanStatus.LOANED) { "이미 반납된 대출입니다" }
        bookCopy.returned()
        status = LoanStatus.RETURNED
        closedAt = returnDate
    }

    fun loseCopy(reportDate: LocalDate = LocalDate.now()) {
        check(status == LoanStatus.LOANED) { "대출 중인 건만 분실 처리할 수 있습니다" }
        bookCopy.markAsLost()
        status = LoanStatus.LOST
        closedAt = reportDate
    }

    fun extend(baseDate: LocalDate = LocalDate.now()) {
        check(status == LoanStatus.LOANED) { "대출 중인 도서만 연장할 수 있습니다" }
        check(extensionCount < MAX_EXTENSION_COUNT) { "연장은 최대 ${MAX_EXTENSION_COUNT}회까지 가능합니다" }
        check(!isOverdue(baseDate)) { "연체된 도서는 연장할 수 없습니다" }

        dueDate = dueDate.plusDays(EXTENSION_DAYS)
        extensionCount++
    }

    companion object {
        const val LOAN_PERIOD_DAYS = 14L
        const val EXTENSION_DAYS = 7L
        const val MAX_EXTENSION_COUNT = 2

        fun create(
            user: User,
            bookCopy: BookCopy,
            loanedAt: LocalDate = LocalDate.now(),
        ): Loan {
            bookCopy.loanOut()
            return Loan(
                user = user,
                bookCopy = bookCopy,
                loanedAt = loanedAt,
                dueDate = loanedAt.plusDays(LOAN_PERIOD_DAYS),
            )
        }
    }
}

enum class LoanStatus(val label: String) {
    LOANED("대출 중"),
    RETURNED("반납 완료"),
    LOST("분실"),
}