package com.knowave.bookhub.domains.user.controller

import com.knowave.bookhub.common.dto.PageResponse
import com.knowave.bookhub.common.security.CurrentUser
import com.knowave.bookhub.common.security.UserPrincipal
import com.knowave.bookhub.domains.user.controller.dto.ApprovedLibrarianResponse
import com.knowave.bookhub.domains.user.controller.dto.PendingLibrarianResponse
import com.knowave.bookhub.domains.user.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.*

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

    // 동작 확인용
    @GetMapping("/me")
    fun getMe(@CurrentUser user: UserPrincipal) : UserPrincipal = user
}