package com.knowave.bookhub.domains.auth.service

import com.knowave.bookhub.domains.auth.service.dto.SignUpLibrarianCommand
import com.knowave.bookhub.domains.auth.service.dto.SignUpMemberCommand
import com.knowave.bookhub.domains.user.service.UserService
import com.knowave.bookhub.domains.user.service.dto.UserResult
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) : AuthService {

    override fun signUpMember(command: SignUpMemberCommand): UserResult =
        userService.createMember(command.toCreateCommand(encodedPassword(command.password)))

    override fun signUpLibrarian(command: SignUpLibrarianCommand): UserResult =
        userService.createLibrarian(command.toCreateCommand(encodedPassword(command.password)))

    private fun encodedPassword(password: String): String {
        return requireNotNull(passwordEncoder.encode(password)) {
            "비밀번호 암호화에 실패했습니다."
        }
    }
}