package com.knowave.bookhub.domains.library.repository

import com.knowave.bookhub.domains.library.entity.BookCopy
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*

interface BookCopyRepository : JpaRepository<BookCopy, UUID> {

    fun existsByLibraryIdAndCallNumber(libraryId: UUID, callNumber: String): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockById(id: UUID): BookCopy?
}
