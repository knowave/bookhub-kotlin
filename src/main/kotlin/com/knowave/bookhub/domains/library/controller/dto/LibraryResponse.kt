package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.LibraryResult
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class LibraryResponse(
    @field:Schema(description = "지점 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID,
    @field:Schema(description = "지점명", example = "중앙도서관")
    val name: String,
    @field:Schema(description = "지점 주소", example = "서울시 중구 세종대로 110")
    val address: String,
    @field:Schema(description = "지점 전화번호", example = "02-1234-5678")
    val phone: String
) {
    companion object {

        fun from(result: LibraryResult) = LibraryResponse(
            id = result.id,
            name = result.name,
            address = result.address,
            phone = result.phone
        )
    }
}
