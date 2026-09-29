package com.knowave.bookhub.fixture

import com.knowave.bookhub.domains.library.entity.Library
import java.time.Instant
import java.util.UUID

fun createLibrary(
    id: UUID = UUID.randomUUID(),
    name: String = "중앙도서관",
    address: String = "서울특별시 강남구 테헤란로 1",
    phone: String = "02-1234-5678",
    createdAt: Instant = Instant.now(),
): Library = Library(
    name = name,
    address = address,
    phone = phone,
).apply {
    this.id = id
    this.createdAt = createdAt
    this.updatedAt = createdAt
}