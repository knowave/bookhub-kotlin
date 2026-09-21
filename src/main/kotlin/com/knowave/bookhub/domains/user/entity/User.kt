package com.knowave.bookhub.domains.user.entity

import com.knowave.bookhub.common.entity.BaseEntity
import com.knowave.bookhub.domains.library.entity.Library
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
    name = "users",
    comment = "서비스 이용자",
    indexes = [Index(name = "idx_users_email", columnList = "email", unique = true)],
)
class User(

    @Column(nullable = false, length = 100, comment = "이메일. 로그인 식별자로 사용한다")
    val email: String,

    @Column(nullable = false, length = 100, comment = "암호화된 비밀번호. 평문을 저장하지 않는다")
    var password: String,

    @Column(nullable = false, length = 50, comment = "이용자명")
    var name: String,

    ) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "권한. 접근 가능한 기능을 결정한다")
    var role: UserRole = UserRole.MEMBER
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "계정 상태. PENDING은 로그인 불가")
    var status: UserStatus = UserStatus.ACTIVE
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", comment = "사서의 소속 지점. MEMBER·ADMIN은 null")
    var library: Library? = null
        protected set

    fun changePassword(encodedPassword: String) {
        this.password = encodedPassword
    }

    fun changeName(name: String) {
        this.name = name
    }

    fun approve() {
        check(status == UserStatus.PENDING) { "승인 대기 상태가 아닙니다" }
        status = UserStatus.ACTIVE
    }

    companion object {
        fun member(email: String, password: String, name: String): User {
            return User(email, password, name).apply {
                role = UserRole.MEMBER
                status = UserStatus.ACTIVE
            }
        }

        fun librarian(email: String, password: String, name: String, library: Library): User {
            return User(email, password, name).apply {
                role = UserRole.LIBRARIAN
                status = UserStatus.PENDING
                this.library = library
            }
        }
    }
}

enum class UserRole(val label: String) {
    MEMBER("일반 회원"),
    LIBRARIAN("사서"),
    ADMIN("관리자"),
}

enum class UserStatus(val label: String) {
    PENDING("승인 대기"),
    ACTIVE("활성"),
}