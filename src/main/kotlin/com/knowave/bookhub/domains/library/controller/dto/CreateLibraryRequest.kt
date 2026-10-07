package com.knowave.bookhub.domains.library.controller.dto

import com.knowave.bookhub.domains.library.service.dto.CreateLibraryCommand
import jakarta.validation.constraints.*

data class CreateLibraryRequest(
    @field:NotBlank(message = "name은 필수값입니다.")
    @field:Size(min = 2, max = 20, message = "name은 최소 2자 이상 최대 20자 이하입니다.")
    val name: String,

    @field:NotBlank(message = "address는 필수값입니다.")
    val address: String,

    @field:NotBlank(message = "phone은 필수값입니다.")
    val phone: String
) {

    fun toCommand() = CreateLibraryCommand(
        name = name,
        address = address,
        phone = phone
    )
}