package com.knowave.bookhub.domains.auth.controller

import com.knowave.bookhub.domains.auth.controller.dto.SignUpLibrarianRequest
import com.knowave.bookhub.domains.auth.controller.dto.SignUpMemberRequest
import com.knowave.bookhub.domains.auth.controller.dto.SignUpResponse
import com.knowave.bookhub.domains.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/sign-up/member")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUpMember(@RequestBody() @Valid() request: SignUpMemberRequest): SignUpResponse =
        SignUpResponse.from(authService.signUpMember(request.toCommand()))

    @PostMapping("/sign-up/librarian")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUpLibrarian(@RequestBody() @Valid() request: SignUpLibrarianRequest): SignUpResponse =
        SignUpResponse.from(authService.signUpLibrarian(request.toCommand()))
}