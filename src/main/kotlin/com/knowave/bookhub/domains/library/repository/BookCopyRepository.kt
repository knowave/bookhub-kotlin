package com.knowave.bookhub.domains.library.repository

import com.knowave.bookhub.domains.library.entity.BookCopy
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BookCopyRepository : JpaRepository<BookCopy, UUID> {

    fun existsByLibraryIdAndCallNumber(libraryId: UUID, callNumber: String): Boolean
}
