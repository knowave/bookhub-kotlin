package com.knowave.bookhub.domains.library.service.dto

data class CreateLibraryCommand(
    val name: String,
    val address: String,
    val phone: String
)
