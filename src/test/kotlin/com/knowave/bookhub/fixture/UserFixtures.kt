package com.knowave.bookhub.fixture

import com.knowave.bookhub.domains.library.entity.Library
import com.knowave.bookhub.domains.user.entity.User
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

private val sequence = AtomicInteger(0)

private fun nextEmail() = "user${sequence.incrementAndGet()}@example.com"

fun createMember(
    id: UUID = UUID.randomUUID(),
    email: String = nextEmail(),
    password: String = "encoded-password",
    name: String = "김독자",
    createdAt: Instant = Instant.now(),
): User = User.member(
    email = email,
    password = password,
    name = name,
).apply {
    this.id = id
    this.createdAt = createdAt
    this.updatedAt = createdAt
}

fun createPendingLibrarian(
    id: UUID = UUID.randomUUID(),
    email: String = nextEmail(),
    password: String = "encoded-password",
    name: String = "박사서",
    library: Library = createLibrary(),
    createdAt: Instant = Instant.now(),
): User = User.librarian(
    email = email,
    password = password,
    name = name,
    library = library,
).apply {
    this.id = id
    this.createdAt = createdAt
    this.updatedAt = createdAt
}

fun createActiveLibrarian(
    id: UUID = UUID.randomUUID(),
    email: String = nextEmail(),
    password: String = "encoded-password",
    name: String = "박사서",
    library: Library = createLibrary(),
    createdAt: Instant = Instant.now(),
): User = createPendingLibrarian(
    id = id,
    email = email,
    password = password,
    name = name,
    library = library,
    createdAt = createdAt,
).apply { approve() }

fun User.asPersisted(
    id: UUID = UUID.randomUUID(),
    createdAt: Instant = Instant.now()
): User = apply {
    this.id = id
    this.createdAt = createdAt
    this.updatedAt = createdAt
}