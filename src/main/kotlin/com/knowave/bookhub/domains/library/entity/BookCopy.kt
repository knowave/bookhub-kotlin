package com.knowave.bookhub.domains.library.entity

import com.knowave.bookhub.common.entity.BaseEntity
import com.knowave.bookhub.domains.book.entity.Book
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "book_copies",
    comment = "서가에 꽂혀 있는 실제 도서 한 권. 대출의 대상이 되는 단위",
    indexes = [
        Index(name = "idx_copies_call_number", columnList = "call_number", unique = true),
        Index(name = "idx_copies_lookup", columnList = "library_id, book_id, status"),
    ],
)
class BookCopy(

    @Column(name = "call_number", nullable = false, length = 50, comment = "청구기호. 지점 내에서 실물을 식별하는 값")
    val callNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, comment = "이 복본이 어떤 도서인지")
    val book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false, comment = "이 복본이 비치된 지점")
    val library: Library,

    ) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "복본 상태. AVAILABLE만 대출 가능")
    var status: BookCopyStatus = BookCopyStatus.AVAILABLE
        protected set

    val isLoanable: Boolean get() = status == BookCopyStatus.AVAILABLE

    fun loanOut() {
        check(status == BookCopyStatus.AVAILABLE) { "대출할 수 없는 상태입니다: ${status.label}" }
        status = BookCopyStatus.LOANED
    }

    fun returned() {
        check(status == BookCopyStatus.LOANED) { "대출 중인 도서만 반납할 수 있습니다" }
        status = BookCopyStatus.AVAILABLE
    }

    fun markAsDamaged() {
        check(status == BookCopyStatus.AVAILABLE) { "파손 처리할 수 없는 상태입니다: ${status.label}" }
        status = BookCopyStatus.DAMAGED
    }

    fun markAsLost() {
        check(status == BookCopyStatus.AVAILABLE || status == BookCopyStatus.LOANED) {
            "분실 처리할 수 없는 상태입니다: ${status.label}"
        }
        status = BookCopyStatus.LOST
    }

    fun repair() {
        check(status == BookCopyStatus.DAMAGED) { "파손된 복본만 수선할 수 있습니다: ${status.label}" }
        status = BookCopyStatus.AVAILABLE
    }
}

enum class BookCopyStatus(val label: String) {
    AVAILABLE("대출 가능"),
    LOANED("대출 중"),
    DAMAGED("파손"),
    LOST("분실"),
}