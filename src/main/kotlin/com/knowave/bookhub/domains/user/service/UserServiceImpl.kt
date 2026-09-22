package com.knowave.bookhub.domains.user.service

import com.knowave.bookhub.common.exception.EmailDuplicatedException
import com.knowave.bookhub.common.exception.UserNotFoundException
import com.knowave.bookhub.domains.library.service.LibraryService
import com.knowave.bookhub.domains.user.entity.User
import com.knowave.bookhub.domains.user.entity.UserRole
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.repository.UserRepository
import com.knowave.bookhub.domains.user.service.dto.CreateLibrarianCommand
import com.knowave.bookhub.domains.user.service.dto.CreateMemberCommand
import com.knowave.bookhub.domains.user.service.dto.UserResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val libraryService: LibraryService,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    @Transactional
    override fun createMember(command: CreateMemberCommand): UserResult {
        if (userRepository.existsByEmail(command.email)) {
            throw EmailDuplicatedException(command.email)
        }

        val user = User.member(
            email = command.email,
            password = command.encodedPassword,
            name = command.name,
        )
        return UserResult.from(userRepository.save(user))
    }

    @Transactional
    override fun createLibrarian(command: CreateLibrarianCommand): UserResult {
        if (userRepository.existsByEmail(command.email)) {
            throw EmailDuplicatedException(command.email)
        }

        val library = libraryService.getLibraryEntity(command.libraryId)
        val user = User.librarian(
            email = command.email,
            password = command.encodedPassword,
            name = command.name,
            library = library,
        )
        return UserResult.from(userRepository.save(user))
    }

    @Transactional
    override fun approveLibrarian(librarianId: UUID): UserResult {
        val user = userRepository.findWithLibraryById(librarianId)
            ?: throw UserNotFoundException(librarianId)
        user.approve()
        return UserResult.from(user)
    }

    override fun getUser(userId: UUID): UserResult =
        UserResult.from(getUserEntity(userId))

    override fun getPendingLibrarians(pageable: Pageable): Page<UserResult> =
        userRepository.findAllByRoleAndStatus(
            role = UserRole.LIBRARIAN,
            status = UserStatus.PENDING,
            pageable = pageable
        ).map { user -> UserResult.from(user) }

    override fun getUserEntity(userId: UUID): User =
        userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException(userId)

    override fun authenticate(email: String, rawPassword: String): UserResult? {
        val user = userRepository.findByEmail(email) ?: return null
        if (!passwordEncoder.matches(rawPassword, user.password)) return null

        return UserResult.from(user)
    }
}