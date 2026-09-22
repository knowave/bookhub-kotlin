package com.knowave.bookhub.domains.library.service

import com.knowave.bookhub.domains.library.entity.Library
import java.util.UUID

interface LibraryService {

    /**
     * 연관관계를 맺기 위해 엔티티를 반환한다.
     * 존재하지 않으면 LibraryNotFoundException.
     */
    fun getLibraryEntity(libraryId: UUID): Library
}