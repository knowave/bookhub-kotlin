package com.knowave.bookhub.domains.library.entity

import com.knowave.bookhub.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(
    name = "libraries",
    comment = "도서관 지점"
)
class Library(

    @Column(nullable = false, length = 100, comment = "지점명")
    var name: String,

    @Column(nullable = false, length = 300, comment = "지점 주소")
    var address: String,

    @Column(nullable = false, length = 20, comment = "대표 전화번호")
    var phone: String,
) : BaseEntity()