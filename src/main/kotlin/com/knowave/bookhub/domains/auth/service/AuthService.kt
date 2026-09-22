package com.knowave.bookhub.domains.auth.service

import com.knowave.bookhub.domains.auth.service.dto.SignUpLibrarianCommand
import com.knowave.bookhub.domains.auth.service.dto.SignUpMemberCommand
import com.knowave.bookhub.domains.user.service.dto.UserResult

interface AuthService {

    fun signUpMember(command: SignUpMemberCommand): UserResult

    fun signUpLibrarian(command: SignUpLibrarianCommand): UserResult
}