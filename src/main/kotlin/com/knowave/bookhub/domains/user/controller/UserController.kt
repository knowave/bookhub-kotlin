package com.knowave.bookhub.domains.user.controller

import com.knowave.bookhub.common.dto.PageResponse
import com.knowave.bookhub.domains.user.controller.dto.PendingLibrarianResponse
import com.knowave.bookhub.domains.user.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/librarians/pending")
    fun getManyPendingLibrarians(pageable: Pageable): PageResponse<PendingLibrarianResponse> =
        PageResponse.from(userService.getManyPendingLibrarians(pageable)) {
            PendingLibrarianResponse.from(it)
        }
}