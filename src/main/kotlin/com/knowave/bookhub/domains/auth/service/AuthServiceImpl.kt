package com.knowave.bookhub.domains.auth.service

import com.knowave.bookhub.common.security.JwtProvider
import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.auth.exception.InvalidCredentialsException
import com.knowave.bookhub.domains.auth.service.dto.LoginCommand
import com.knowave.bookhub.domains.auth.service.dto.LoginResult
import com.knowave.bookhub.domains.auth.service.dto.SignUpLibrarianCommand
import com.knowave.bookhub.domains.auth.service.dto.SignUpMemberCommand
import com.knowave.bookhub.domains.user.entity.UserStatus
import com.knowave.bookhub.domains.user.exception.UserNotActiveException
import com.knowave.bookhub.domains.user.service.UserService
import com.knowave.bookhub.domains.user.service.dto.UserResult
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) : AuthService {

    override fun signUpMember(command: SignUpMemberCommand): UserResult =
        userService.createMember(command.toCreateCommand(encodedPassword(command.password)))

    override fun signUpLibrarian(command: SignUpLibrarianCommand): UserResult =
        userService.createLibrarian(command.toCreateCommand(encodedPassword(command.password)))

    override fun login(command: LoginCommand): LoginResult {
        val user = userService.authenticate(email = command.email, rawPassword = command.password)
            ?: throw InvalidCredentialsException()

        if (user.status != UserStatus.ACTIVE) {
            throw UserNotActiveException(userId = user.id)
        }

        val principal = UserPrincipal(
            id = user.id,
            role = user.role,
            libraryId = user.library?.id
        )

        val accessToken = jwtProvider.createAccessToken(principal)
        val refreshToken = jwtProvider.createRefreshToken(principal)

        return LoginResult(accessToken, refreshToken)
    }

    private fun encodedPassword(password: String): String {
        return requireNotNull(passwordEncoder.encode(password)) {
            "비밀번호 암호화에 실패했습니다."
        }
    }
}