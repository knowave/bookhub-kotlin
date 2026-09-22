package com.knowave.bookhub.domains.user.service

import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.service.dto.CreateLibrarianCommand
import com.knowave.bookhub.domains.user.service.dto.CreateMemberCommand
import com.knowave.bookhub.domains.user.service.dto.UserResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface UserService {

    fun createMember(command: CreateMemberCommand): UserResult

    fun createLibrarian(command: CreateLibrarianCommand): UserResult

    fun approveLibrarian(librarianId: UUID): UserResult

    fun getUser(userId: UUID): UserResult

    fun getPendingLibrarians(pageable: Pageable): Page<UserResult>

    fun getUserEntity(userId: UUID): User

    /**
     * 이메일과 비밀번호가 일치한지 검증한다.
     */
    fun authenticate(email: String, rawPassword: String): UserResult?
}