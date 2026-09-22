package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.common.exception.LibraryNotFoundException
import com.knowave.bookhub.domains.library.LibraryRepository
import com.knowave.bookhub.domains.library.entity.Library
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LibraryServiceImpl(
    private val libraryRepository: LibraryRepository
) : LibraryService {

    override fun getLibraryEntity(libraryId: UUID): Library =
        libraryRepository.findByIdOrNull(libraryId)
            ?: throw LibraryNotFoundException(libraryId)
}