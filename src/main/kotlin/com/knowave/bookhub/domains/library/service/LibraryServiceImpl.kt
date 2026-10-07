package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.common.exception.LibraryNotFoundException
import com.knowave.bookhub.domains.library.repository.LibraryRepository
import com.knowave.bookhub.domains.library.entity.Library
import com.knowave.bookhub.domains.library.service.dto.CreateLibraryCommand
import com.knowave.bookhub.domains.library.service.dto.LibraryResult
import com.knowave.bookhub.domains.library.service.dto.UpdateContactCommand
import com.knowave.bookhub.domains.library.service.dto.UpdateNameCommand
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LibraryServiceImpl(
    private val libraryRepository: LibraryRepository
) : LibraryService {

    override fun getLibraryEntity(libraryId: UUID): Library =
        libraryRepository.findByIdOrNull(libraryId)
            ?: throw LibraryNotFoundException(libraryId)

    @Transactional
    override fun createLibrary(command: CreateLibraryCommand): LibraryResult {
        val library = Library(
            name = command.name,
            address = command.address,
            phone = command.phone,
        )

        return LibraryResult.from(libraryRepository.save(library))
    }

    @Transactional
    override fun updateName(command: UpdateNameCommand): LibraryResult {
        val library = getLibraryEntity(command.libraryId)

        library.changeName(command.name)
        return LibraryResult.from(library)
    }

    @Transactional
    override fun updateContact(command: UpdateContactCommand): LibraryResult {
        val library = getLibraryEntity(command.libraryId)

        library.changeContact(address = command.address, phone = command.phone)
        return LibraryResult.from(library)
    }

    override fun getLibraries(): List<LibraryResult> =
        libraryRepository.findAll().map { LibraryResult.from(it) }
}