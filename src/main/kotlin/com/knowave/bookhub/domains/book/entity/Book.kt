package com.knowave.bookhub.domains.book.entity

import com.knowave.bookhub.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "books",
    comment = "도서 서지정보. 물리적 실물이 아닌 카탈로그 단위",
    indexes = [
        Index(name = "idx_books_isbn", columnList = "isbn", unique = true),
        Index(name = "idx_books_title", columnList = "title"),
    ],
)
class Book(

    @Column(nullable = false, length = 20, comment = "국제표준도서번호. 도서를 식별하는 고유값")
    val isbn: String,

    @Column(nullable = false, length = 300, comment = "도서명")
    var title: String,

    @Column(nullable = false, length = 200, comment = "저자명. 공저는 쉼표로 구분")
    var author: String,

    @Column(nullable = false, length = 100, comment = "출판사명")
    var publisher: String,

    @Column(nullable = false, comment = "출간일")
    var publishedAt: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, comment = "도서 분류")
    var category: Category,
) : BaseEntity() {

    @Column(length = 2000, comment = "도서 소개. 등록되지 않을 수 있다")
    var description: String? = null

    fun changeCategory(category: Category) {
        this.category = category
    }

    fun updateDescription(description: String?) {
        this.description = description
    }
}