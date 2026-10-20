package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.CreateLibraryCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class CreateLibraryRequest(
    @field:Schema(description = "지점명", example = "중앙도서관")
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(min = 2, max = 20, message = "name은 최소 2자 이상 최대 20자 이하입니다.")
    val name: String,

    @field:Schema(description = "지점 주소", example = "서울시 중구 세종대로 110")
    @field:NotBlank(message = "address는 필수값입니다.")
    val address: String,

    @field:Schema(description = "지점 전화번호", example = "02-1234-5678")
    @field:NotBlank(message = "phone은 필수값입니다.")
    val phone: String
) {

    fun toCommand() = CreateLibraryCommand(
        name = name,
        address = address,
        phone = phone
    )
}