package com.knowave.bookhub.domains.user.controller

import com.knowave.bookhub.common.dto.PageResponse
import com.knowave.bookhub.domains.user.controller.dto.ApprovedLibrarianResponse
import com.knowave.bookhub.domains.user.controller.dto.PendingLibrarianResponse
import com.knowave.bookhub.domains.user.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    // TODO: ADMIN 권한 검증 필요 (→ P-4). JWT 도입 시 추가
    @GetMapping("/librarians/pending")
    fun getManyPendingLibrarians(pageable: Pageable): PageResponse<PendingLibrarianResponse> =
        PageResponse.from(userService.getManyPendingLibrarians(pageable)) {
            PendingLibrarianResponse.from(it)
        }

    // TODO: ADMIN 권한 검증 필요 (→ P-4). JWT 도입 시 추가
    @PostMapping("/librarians/{id}/approval")
    fun approvedLibrarian(@PathVariable id: UUID): ApprovedLibrarianResponse =
        ApprovedLibrarianResponse.from(userService.approveLibrarian(id))
}