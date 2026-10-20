package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.UpdateContactCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateLibraryContactRequest(
    @field:Schema(description = "변경할 지점 주소", example = "서울시 중구 세종대로 110")
    val address: String?,
    @field:Schema(description = "변경할 지점 전화번호", example = "02-1234-5678")
    val phone: String?
) {
    fun toCommand(libraryId: UUID): UpdateContactCommand =
        UpdateContactCommand(
            libraryId = libraryId,
            address = address,
            phone = phone
        )
}
