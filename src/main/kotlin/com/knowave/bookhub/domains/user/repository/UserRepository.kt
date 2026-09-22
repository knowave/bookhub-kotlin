package com.knowave.bookhub.domains.user.repository

import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = ["library"])
    fun findByEmail(email: String): User?

    @EntityGraph(attributePaths = ["library"])
    fun findWithLibraryById(id: UUID): User?

    fun existsByEmail(email: String): Boolean

    /**
     * 승인 대기 중인 사서 목록
     */
    @EntityGraph(attributePaths = ["library"])
    fun findAllByRoleAndStatus(role: UserRole, status: UserStatus, pageable: Pageable): Page<User>
}