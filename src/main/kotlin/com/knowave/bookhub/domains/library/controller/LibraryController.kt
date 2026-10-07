package com.knowave.bookhub.domains.library.controller

import com.knowave.bookhub.domains.library.controller.dto.CreateLibraryRequest
import com.knowave.bookhub.domains.library.controller.dto.LibraryResponse
import com.knowave.bookhub.domains.library.controller.dto.UpdateLibraryContactRequest
import com.knowave.bookhub.domains.library.controller.dto.UpdateLibraryNameRequest
import com.knowave.bookhub.domains.library.service.LibraryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/libraries")
class LibraryController(
    private val libraryService: LibraryService
) {

    // TODO: ADMIN 권한 검증 필요
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLibrary(@RequestBody @Valid request: CreateLibraryRequest): LibraryResponse =
        LibraryResponse.from(libraryService.createLibrary(request.toCommand()))

    // TODO: ADMIN 권한 검증 필요
    @PatchMapping("/{id}/name")
    fun updateLibraryName(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UpdateLibraryNameRequest
    ): LibraryResponse =
        LibraryResponse.from(libraryService.updateName(request.toCommand(libraryId = id)))

    // TODO: LIBRARIAN 권한 검증 필요
    @PatchMapping("/{id}/contact")
    fun updateLibraryContact(
        @PathVariable id: UUID,
        @RequestBody request: UpdateLibraryContactRequest
    ): LibraryResponse =
        LibraryResponse.from(libraryService.updateContact(request.toCommand(libraryId = id)))

    @GetMapping()
    fun getManyLibrary() : List<LibraryResponse> =
        libraryService.getLibraries().map { LibraryResponse.from(it) }
}